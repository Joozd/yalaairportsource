package nl.joozd.airportsource.yasinterfaces

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface AirportData{
    val airports: List<Airport>
    val version: Long
}

/**
 * Describes the changes required to update airport data from one version to another.
 *
 * Airports in [airports] are added or replace airports with the same ID in the previous
 * version. Airports whose IDs are listed in [removedAirportIDs] are removed.
 *
 * An airport cannot be both present in [airports] and listed in [removedAirportIDs].
 *
 * @property airports airports that were added or changed since [previousVersion].
 * @property removedAirportIDs IDs of airports that were removed since [previousVersion].
 * @property version version produced by applying this delta.
 * @property previousVersion version to which this delta applies.
 */
@Serializable
@SerialName(ClassNames.AIRPORT_DATA_TYPE_PARTIAL)
data class AirportDataDelta(
    override val airports: List<Airport>,
    val removedAirportIDs: Set<Long>,
    override val version: Long,
    val previousVersion: Long
) : AirportData {
    init {
        require(airports.none { it.id in removedAirportIDs }) {
            "AirportDataDelta cannot contain the same airport in both airports and removedAirportIDs"
        }
    }
}

@Serializable
@SerialName(ClassNames.AIRPORT_DATA_TYPE_FULL)
data class FullAirportData(
    override val airports: List<Airport>,
    override val version: Long,
): AirportData