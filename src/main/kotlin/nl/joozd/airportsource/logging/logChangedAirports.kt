package nl.joozd.airportsource.logging

import nl.joozd.airportsource.yasinterfaces.AirportDataDelta
import nl.joozd.airportsource.yasinterfaces.FullAirportData
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("changedAirports")

internal fun logChangedAirports(delta: AirportDataDelta, old: FullAirportData){
    val newIDs = delta.airports.map { it.id }.toSet()
    val oldAirportsMap = old.airports.filter { it.id in newIDs }.associateBy { it.id }

    delta.airports.map { oldAirportsMap[it.id] to it }
        .forEach { oldToNew ->
            logger.info("${oldToNew.first} -> ${oldToNew.second}")
        }
}