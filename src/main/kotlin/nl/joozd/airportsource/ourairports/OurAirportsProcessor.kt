package nl.joozd.airportsource.ourairports

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import nl.joozd.airportsource.Processor
import nl.joozd.airportsource.args.Args
import nl.joozd.airportsource.io.getSerializableFromGzipJson
import nl.joozd.airportsource.io.writeAirportDataAsGzipJson
import nl.joozd.airportsource.structuring.getDelta
import nl.joozd.airportsource.yasinterfaces.*
import org.slf4j.LoggerFactory
import java.net.URI
import kotlin.io.path.deleteIfExists
import kotlin.io.path.readText
import kotlin.io.path.writeText

private val logger = LoggerFactory.getLogger("OurAirportsProcessor")

/**
 * Downloads the latest OurAirports airport data and maintains the downloadable
 * full and delta data files.
 *
 * Each update creates a new full data file and, when previous data is available,
 * a delta from the previous version to the new version. Obsolete files are kept
 * for a grace period of one update cycle before being removed, allowing clients
 * that obtained the previous manifest to finish their download.
 */
class OurAirportsProcessor : Processor {

    /**
     * Downloads and processes the latest OurAirports data.
     *
     * Existing obsolete files are cleaned up first, after which the new full and
     * delta data are generated and a new manifest is written.
     */
    override fun downloadAndProcess(args: Args) {
        logger.info("Starting OurAirports update in {}", args.outputDir.toAbsolutePath())

        val currentManifest = getCurrentManifest(args) ?: AirportDataManifest().also {
            logger.info("No existing manifest found; starting with an empty manifest")
        }

        val cleanedManifest = cleanup(args, currentManifest)
        val newManifest = update(args, cleanedManifest)

        writeManifest(args, newManifest)

        logger.info(
            "OurAirports update completed; current version={}, earliest delta={}",
            newManifest.currentVersion,
            newManifest.earliestDelta
        )
    }

    /**
     * Reads the currently published airport data manifest.
     *
     * @return the current manifest, or `null` when no manifest exists yet.
     */
    private fun getCurrentManifest(args: Args): AirportDataManifest? {
        val manifestPath = args.outputDir.resolve(AirportDataManifest.MANIFEST_FILE_NAME)

        if (!manifestPath.toFile().exists()) {
            return null
        }

        val manifest = Json.decodeFromString<AirportDataManifest>(manifestPath.readText())

        logger.info(
            "Loaded manifest; current version={}, earliest delta={}, files={}",
            manifest.currentVersion,
            manifest.earliestDelta,
            manifest.files.size
        )
        logger.debug("Current manifest: {}", manifest)

        return manifest
    }

    /**
     * Removes files that have passed their grace period.
     *
     * Files are deliberately retained for one update cycle after they stop being
     * required by the current manifest. This prevents a race where a client has
     * already obtained an older manifest but has not yet downloaded its files.
     *
     * @return a copy of [currentManifest] without entries for files that were
     * eligible for deletion.
     */
    private fun cleanup(
        args: Args,
        currentManifest: AirportDataManifest,
    ): AirportDataManifest {
        val filesToDelete = currentManifest.files
            .filter { it.canBeDeleted(currentManifest) }
            .toSet()

        if (filesToDelete.isEmpty()) {
            logger.debug("No obsolete airport data files to clean up")
            return currentManifest
        }

        logger.info("Cleaning up {} obsolete airport data file(s)", filesToDelete.size)

        for (fileToDelete in filesToDelete) {
            val path = args.outputDir.resolve(fileToDelete.filename)

            if (path.deleteIfExists()) {
                logger.info("Deleted obsolete file {}", fileToDelete.filename)
            } else {
                logger.warn(
                    "Obsolete file {} was listed in the manifest but did not exist on disk",
                    fileToDelete.filename
                )
            }
        }

        return currentManifest.copy(
            files = currentManifest.files.filter { it !in filesToDelete }
        )
    }

    /**
     * Creates the latest full airport data, optionally creates a delta from the
     * previous version, writes both to disk, and updates the manifest.
     *
     * @return the updated manifest describing the newly written data.
     */
    private fun update(
        args: Args,
        currentManifest: AirportDataManifest,
    ): AirportDataManifest {
        val current = getCurrentFullAirportData(args, currentManifest)
        val new = getNewAirportData()

        logger.info(
            "Processing airport data update from version {} to {}",
            current?.version,
            new.version
        )

        //getDelta returns null if run multiple times on same full version, so no duplicates
        val delta = current?.let { getDelta(it, new) }

        val newAirportDataFile = new.let {
            val newFullFileName = "full-airport-data-${new.version}$JSON_GZIP_EXTENSION"
            val newFullFilePath = args.outputDir.resolve(newFullFileName)

            logger.info("Writing full airport data to {}", newFullFileName)

            @OptIn(ExperimentalSerializationApi::class)
            val writeResult = writeAirportDataAsGzipJson(new, newFullFilePath)

            logger.info(
                "Wrote full airport data; version={}, size={} bytes",
                new.version,
                writeResult.size
            )
            logger.debug(
                "Full airport data SHA-256: {}",
                writeResult.sha256
            )

            FullAirportDataFile(
                filename = newFullFileName,
                version = new.version,
                sha256 = writeResult.sha256,
                size = writeResult.size
            )
        }

        val newDeltaFile = delta?.let { d ->
            val newDeltaFileName =
                "airport-data-delta-${d.previousVersion}-${d.version}$JSON_GZIP_EXTENSION"
            val newDeltaFilePath = args.outputDir.resolve(newDeltaFileName)

            logger.info(
                "Writing airport delta {} -> {} to {}",
                d.previousVersion,
                d.version,
                newDeltaFileName
            )

            @OptIn(ExperimentalSerializationApi::class)
            val writeResult = writeAirportDataAsGzipJson(d, newDeltaFilePath)

            logger.info(
                "Wrote airport delta; {} -> {}, size={} bytes",
                d.previousVersion,
                d.version,
                writeResult.size
            )
            logger.debug(
                "Airport delta SHA-256: {}",
                writeResult.sha256
            )

            AirportDeltaDataFile(
                filename = newDeltaFileName,
                version = d.version,
                previousVersion = d.previousVersion,
                sha256 = writeResult.sha256,
                size = writeResult.size
            )
        } ?: run {
            logger.info("No previous full airport data available; no delta will be generated")
            null
        }

        val updatedManifest = currentManifest.copy(
            currentVersion = new.version,
            files = currentManifest.files + listOfNotNull(
                newAirportDataFile,
                newDeltaFile
            )
        )

        return updateEarliestDelta(args, updatedManifest)
    }

    /**
     * Loads the full airport data for the manifest's current version.
     *
     * @return the current full airport data, or `null` when no matching full
     * data file exists in the manifest.
     *
     * @throws IllegalStateException if the referenced file does not contain
     * [FullAirportData].
     */
    private fun getCurrentFullAirportData(
        args: Args,
        manifest: AirportDataManifest,
    ): FullAirportData? {
        val currentAirportDataFile = manifest.files
            .filterIsInstance<FullAirportDataFile>()
            .firstOrNull { it.version == manifest.currentVersion }
            ?: return null.also {
                logger.debug(
                    "No full airport data found for current version {}",
                    manifest.currentVersion
                )
            }

        logger.debug(
            "Loading current full airport data from {}",
            currentAirportDataFile.filename
        )

        val path = args.outputDir.resolve(currentAirportDataFile.filename)
        val deserialized = getSerializableFromGzipJson<AirportData>(path)

        check(deserialized is FullAirportData) {
            "Deserialized wrong type of data; expected FullAirportData but got " +
                    deserialized::class.simpleName
        }

        return deserialized
    }

    /**
     * Downloads and parses the latest airport data from OurAirports.
     */
    private fun getNewAirportData(): FullAirportData {
        logger.info("Downloading latest airport data from OurAirports")

        val uri = URI(OUR_AIRPORTS_AIRPORTS_CSV_DOWNLOAD_LOCATION)
        val downloader = OurAirportsDownloader(uri)
        val airportData = downloader.getFullAirportData()

        logger.info(
            "Downloaded latest airport data; version={}, airports={}",
            airportData.version,
            airportData.airports.size
        )

        return airportData
    }

    /**
     * Determines whether this file has passed its grace period and may be
     * removed before publishing the next manifest.
     */
    private fun AirportDataFile.canBeDeleted(manifest: AirportDataManifest): Boolean =
        when (this) {
            is FullAirportDataFile -> manifest.currentVersion != version
            is AirportDeltaDataFile -> version < (manifest.earliestDelta ?: -1)
        }

    /**
     * Updates the oldest client version for which a complete delta chain is
     * guaranteed to be available.
     *
     * [AirportDataManifest.earliestDelta] represents the version a client
     * currently has, rather than the destination version of the oldest delta.
     * It therefore corresponds to [AirportDeltaDataFile.previousVersion].
     *
     * Delta files older than this value remain in the manifest for the current
     * update cycle and are removed by [cleanup] on the next run. This provides
     * the grace period required for clients using the previous manifest.
     */
    private fun updateEarliestDelta(
        args: Args,
        manifest: AirportDataManifest,
    ): AirportDataManifest {
        val currentDeltas = manifest.files.filterIsInstance<AirportDeltaDataFile>()

        if (currentDeltas.size <= args.maxDeltas) {
            logger.debug(
                "Delta retention limit not exceeded: {} of {}",
                currentDeltas.size,
                args.maxDeltas
            )
            return manifest
        }

        if (args.maxDeltas == 0) {
            logger.info("Delta retention disabled; clearing earliest delta version")
            return manifest.copy(earliestDelta = null)
        }

        val sortedDeltaVersions = currentDeltas
            .map { it.previousVersion }
            .sortedDescending()

        val newEarliest = sortedDeltaVersions[args.maxDeltas - 1]

        if (newEarliest != manifest.earliestDelta) {
            logger.info(
                "Advancing earliest supported delta version from {} to {}",
                manifest.earliestDelta,
                newEarliest
            )
        }

        return manifest.copy(
            earliestDelta = newEarliest
        )
    }

    /**
     * Publishes [manifest] to the configured output directory.
     *
     * The manifest is written last so clients never discover newly generated
     * data files before those files have been completely written.
     */
    private fun writeManifest(
        args: Args,
        manifest: AirportDataManifest,
    ) {
        val outputFile = args.outputDir.resolve(AirportDataManifest.MANIFEST_FILE_NAME)

        logger.info("Writing manifest to {}", outputFile)
        outputFile.writeText(manifest.prettyJson())
        logger.debug("Published manifest: {}", manifest)
    }

    companion object {
        private const val OUR_AIRPORTS_AIRPORTS_CSV_DOWNLOAD_LOCATION =
            "https://davidmegginson.github.io/ourairports-data/airports.csv"

        private const val JSON_GZIP_EXTENSION = ".json.gz"
    }
}