package nl.joozd.airportsource.yasinterfaces

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Describes all airport data files currently available for download.
 *
 * Files with a version equal to or newer than [earliestDelta] are guaranteed
 * to remain available while this manifest is current. Older delta files may
 * still be listed to allow clients using a previously retrieved manifest to
 * complete an update, but their availability is not guaranteed.
 *
 * @property currentVersion The most recent available airport data version.
 * @property files All airport data files available when this manifest was created.
 * @property earliestDelta The earliest delta version whose availability is guaranteed.
 */
@Serializable
data class AirportDataManifest(
    val currentVersion: Long,
    val files: List<AirportDataFile>,
    val earliestDelta: Long,
)

/**
 * Describes a downloadable airport data file.
 */
@Serializable
sealed interface AirportDataFile {
    val path: String
    val version: Long
    val size: Long
    val sha256: String
}

/**
 * Describes a complete airport data file.
 */
@Serializable
@SerialName(ClassNames.AIRPORT_DATA_TYPE_FULL)
data class FullAirportDataFile(
    override val path: String,
    override val version: Long,
    override val size: Long,
    override val sha256: String,
) : AirportDataFile

/**
 * Describes an incremental airport data file.
 */
@Serializable
@SerialName(ClassNames.AIRPORT_DATA_TYPE_PARTIAL)
data class PartialAirportDataFile(
    override val path: String,
    override val version: Long,
    val previousVersion: Long,
    override val size: Long,
    override val sha256: String,
) : AirportDataFile