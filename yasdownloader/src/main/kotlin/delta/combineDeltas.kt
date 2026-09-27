package nl.joozd.yalaairportsource.delta

import nl.joozd.airportsource.yasinterfaces.Airport
import nl.joozd.airportsource.yasinterfaces.AirportDataDelta

/**
 * Combines a chain of [AirportDataDelta] instances into a single delta.
 *
 * The resulting delta represents the net change from the
 * [AirportDataDelta.previousVersion] of the oldest delta to the
 * [AirportDataDelta.version] of the newest delta.
 *
 * Intermediate states are discarded. For each airport ID, only its final state
 * is represented:
 * - an airport that is added or changed and later removed is included only in
 *   [AirportDataDelta.removedAirportIDs];
 * - an airport that is removed and later added or changed is included only in
 *   [AirportDataDelta.airports].
 *
 * The input may be provided in any order, but the deltas must form an unbroken
 * version chain.
 *
 * No ordering of [AirportDataDelta.airports] or
 * [AirportDataDelta.removedAirportIDs] in the resulting delta is guaranteed.
 *
 * @param deltas the deltas to combine.
 * @return the combined delta, or `null` if [deltas] is empty.
 * @throws IllegalArgumentException if the deltas do not form an unbroken version chain, or if the list of deltas is empty.
 */
internal fun mergeDeltas(deltas: List<AirportDataDelta>): AirportDataDelta {
    if (deltas.isEmpty()) throw IllegalArgumentException("Cannot merge an empty list of deltas.")

    val sorted = deltas.sortedBy { it.version }

    requireNoMissingSteps(sorted)

    val versionFrom = sorted.first().previousVersion
    val versionTo = sorted.last().version

    // All Longs here are Airport.id.
    val changedAirportsMap = HashMap<Long, Airport>()
    val removedIDs = mutableSetOf<Long>()

    @Suppress("DestructuringDeclaration") // Destructuring is less clear here.
    for (delta in sorted) {
        for (airport in delta.airports) {
            changedAirportsMap[airport.id] = airport
            removedIDs.remove(airport.id)
        }
        for (removedID in delta.removedAirportIDs) {
            changedAirportsMap.remove(removedID)
            removedIDs.add(removedID)
        }
    }

    return AirportDataDelta(
        airports = changedAirportsMap.values.toList(),
        removedAirportIDs = removedIDs,
        version = versionTo,
        previousVersion = versionFrom,
    )
}

/**
 * Verifies that [sortedDeltas] forms an unbroken version chain.
 *
 * Each delta's [AirportDataDelta.previousVersion] must equal the
 * [AirportDataDelta.version] produced by the preceding delta.
 *
 * [sortedDeltas] must be non-empty and sorted in ascending version order.
 *
 * @param sortedDeltas the ordered delta chain to verify.
 * @throws IllegalArgumentException if a step in the version chain is missing.
 */
private fun requireNoMissingSteps(sortedDeltas: List<AirportDataDelta>) {
    var expectedVersion = sortedDeltas.first().previousVersion

    @Suppress("DestructuringDeclaration")
    for (delta in sortedDeltas) {
        require(delta.previousVersion == expectedVersion) {
            "Merging deltas requires an unbroken chain; " +
                    "expected a delta from version $expectedVersion, " +
                    "but found one from ${delta.previousVersion}."
        }
        expectedVersion = delta.version
    }
}