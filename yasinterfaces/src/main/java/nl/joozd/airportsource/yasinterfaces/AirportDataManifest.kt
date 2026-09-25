package nl.joozd.airportsource.yasinterfaces

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.String

/**
 * Describes all airport data files currently available for download.
 *
 * Files with a version equal to or newer than [earliestDelta] are guaranteed
 * to remain available while this manifest is current. Older delta files may
 * still be listed to allow clients using a previously retrieved manifest to
 * complete an update, but their availability is not guaranteed.
 *
 * @property currentVersion The most recent available airport data version. null if no data present.
 *  This version is not guaranteed to be available, it will be deleted after a grace period (race condition).
 * @property files All airport data files available when this manifest was created.
 * @property earliestDelta The earliest delta version whose availability is guaranteed. Null if no delta's available.
 */
@Serializable
data class AirportDataManifest(
    val currentVersion: Long? = null,
    val files: List<AirportDataFile> = emptyList(),
    val earliestDelta: Long? = null,
){
    fun prettyJson(): String = jsonPretty.encodeToString(this)
    companion object {
        const val MANIFEST_FILE_NAME = "airportDataManifest.json"
        private val jsonPretty by lazy {
            Json { prettyPrint = true}
        }

    }
}

/**
 * Describes a downloadable airport data file.
 */
@Serializable
sealed interface AirportDataFile {
    val filename: String
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
    override val filename: String,
    override val version: Long,
    override val size: Long,
    override val sha256: String,
) : AirportDataFile


/**
 * Describes an incremental airport data file.
 */
@Serializable
@SerialName(ClassNames.AIRPORT_DATA_TYPE_PARTIAL)
data class AirportDeltaDataFile(
    override val filename: String,
    override val version: Long,
    val previousVersion: Long,
    override val size: Long,
    override val sha256: String,
) : AirportDataFile