package nl.joozd.yalaairportsource

import getAirportData
import getManifest
import nl.joozd.airportsource.yasinterfaces.AirportDataDelta
import nl.joozd.airportsource.yasinterfaces.AirportDataManifest
import nl.joozd.airportsource.yasinterfaces.AirportDeltaDataFile
import nl.joozd.airportsource.yasinterfaces.FullAirportData
import nl.joozd.airportsource.yasinterfaces.FullAirportDataFile
import nl.joozd.yalaairportsource.delta.mergeDeltas

/**
 * Downloads airport data updates from the configured YALA airport data source.
 *
 * The downloader determines whether a client can be updated incrementally or
 * requires a complete data set based on the currently published manifest.
 *
 * @param downloadUriPrefix URI prefix from which airport data files are downloaded.
 */
@Suppress("Unused")
class YasDownloader(private val downloadUriPrefix: String = DOWNLOAD_URI_PREFIX) {
    /**
     * Gets the airport data required to update [versionToUpdateFrom] to the
     * current published version.
     *
     * Returns [DownloadResult.UpToDate] when [versionToUpdateFrom] is already
     * current. If a complete delta chain from [versionToUpdateFrom] is available,
     * the required deltas are downloaded and merged into a single
     * [DownloadResult.Incremental]. Otherwise, the current complete airport data
     * set is returned as [DownloadResult.Full].
     *
     * @param versionToUpdateFrom the version of the airport data to update.
     * @return the data required to update to the current published version, or
     * [DownloadResult.UpToDate] if no update is required.
     * @throws IllegalArgumentException if [versionToUpdateFrom] is newer than
     * the current published version.
     * @throws IllegalStateException if the published manifest or downloaded data
     * is inconsistent with the requested update.
     */
    fun getUpdatesFromVersion(versionToUpdateFrom: Long): DownloadResult{
        val manifest = getManifest(downloadUriPrefix)
        require(versionToUpdateFrom <= manifest.currentVersion!!) {
            "Version to update from ($versionToUpdateFrom) is newer than current server version (${manifest.currentVersion})."
        }
        return when {
            versionToUpdateFrom == manifest.currentVersion -> DownloadResult.UpToDate
            versionToUpdateFrom >= (manifest.earliestDelta ?: Long.MAX_VALUE) -> getDeltaUpdate (manifest, versionToUpdateFrom)
            else -> getFullAirportData(manifest)
        }
    }

    /**
     * Downloads the complete airport data set for the current version in [manifest].
     *
     * @param manifest the manifest describing the currently published airport data.
     * @return the current complete airport data set as [DownloadResult.Full].
     * @throws IllegalStateException if the current full data file cannot be found
     * or does not contain [FullAirportData].
     */
    private fun getFullAirportData(manifest: AirportDataManifest): DownloadResult {
        val currentVersion = manifest.currentVersion
        val currentFullAirportDataFile =
            manifest.files
                .filterIsInstance<FullAirportDataFile>()
                .first {it.version == currentVersion}

        val fullAirportData = getAirportData(downloadUriPrefix, currentFullAirportDataFile)
        check(fullAirportData is FullAirportData) { "Expected FullAirportData, but ${fullAirportData::class.simpleName} was downloaded." }
        return DownloadResult.Full(fullAirportData)
    }

    /**
     * Downloads the complete airport data set for the current version.
     *
     * @return the current complete airport data set as [DownloadResult.Full].
     * @throws IllegalStateException if the current full data file cannot be found
     * or does not contain [FullAirportData].
     */
    fun getFullAirportData(): DownloadResult {
        val manifest = getManifest(downloadUriPrefix)
        return getFullAirportData(manifest)
    }

    /**
     * Downloads and merges the delta chain required to update
     * [versionToUpdateFrom] to the current version in [manifest].
     *
     * @param manifest the manifest describing the currently published airport data.
     * @param versionToUpdateFrom the version from which to build the incremental update.
     * @return the merged incremental update as [DownloadResult.Incremental].
     * @throws IllegalArgumentException if [versionToUpdateFrom] is older than the
     * earliest version for which an incremental update is available.
     * @throws IllegalStateException if the required delta files are missing or
     * contain data other than [AirportDataDelta].
     */
    private fun getDeltaUpdate(manifest: AirportDataManifest, versionToUpdateFrom: Long): DownloadResult {
        val earliestPossible = manifest.earliestDelta
        require(earliestPossible != null && versionToUpdateFrom >= earliestPossible) {
            "Attempted to build an incremental update from earlier than earliest supported version. " +
                    "Requested version was $versionToUpdateFrom, earliest supported is $earliestPossible." }

        val deltaFiles = manifest.files
            .filterIsInstance<AirportDeltaDataFile>()
            .filter{ it.previousVersion >= versionToUpdateFrom }


        //sanity checks to catch bugs
        check(deltaFiles.isNotEmpty()) { "No incremental update files were found, but 1 or more were expected."}
        check(deltaFiles.minBy { it.previousVersion }.previousVersion == versionToUpdateFrom) { "Downloaded incremental updates do not match expected versions"}

        val deltas = deltaFiles.map {
            getAirportData(downloadUriPrefix, it).let { data ->
                check(data is AirportDataDelta) { "Expected AirportDataDelta, but ${data::class.simpleName} was downloaded." }
                data
            }
        }

        val mergedDeltas = mergeDeltas(deltas)

        return DownloadResult.Incremental(mergedDeltas)
    }
}