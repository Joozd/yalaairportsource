package nl.joozd.airportsource.structuring

import kotlinx.serialization.json.Json
import nl.joozd.airportsource.yasinterfaces.Airport
import nl.joozd.airportsource.yasinterfaces.AirportData
import nl.joozd.airportsource.yasinterfaces.AirportDataDelta
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("missingAirports")

/**
 * Creates a delta containing the changes required to transform one airport dataset version into another.
 *
 * Airports are matched by their ID. An airport is included in the delta when it does not exist
 * in [oldData], or when its data differs from the corresponding airport in [oldData].
 *
 * @param oldData the previous airport dataset.
 * @param newData the new airport dataset.
 * @return a delta from [oldData] to [newData] containing all added or changed airports.
 */
internal fun getDelta(oldData: AirportData, newData: AirportData): AirportDataDelta? {
    if (oldData.version == newData.version) return null
    val oldMap = oldData.airports.associateBy { it.id }
    val changedAirports = newData.airports.filter {
        val oldAirport = oldMap[it.id]
        oldAirport == null || it != oldAirport
    }
    val removed = removedAirports(oldData, newData).map { it.id }.toSet()

    return AirportDataDelta(
        airports = changedAirports,
        removedAirportIDs = removed,
        previousVersion = oldData.version,
        version = newData.version
    )
}

/**
 * Returns al airports that were present in [oldData] but are missing in [newData].
 */
private fun removedAirports(oldData: AirportData, newData: AirportData): List<Airport> {
    val newIDs = newData.airports.map { it.id}.toSet()
    val missing = oldData.airports.filter { it.id !in newIDs }.also{ removed ->
        for (airport in removed)
            logger.info(
                "Airport removed: {} ({}) - {}",
                airport.name,
                airport.ident,
                Json.encodeToString(airport)
            )
    }
    return missing
}