package nl.joozd.airportsource.ourairports

import nl.joozd.airportsource.yasinterfaces.Airport
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.Reader

/**
 * Parses airport data from an OurAirports CSV stream.
 *
 * Records are parsed lazily and are not loaded into memory as a complete
 * collection. The caller retains ownership of [reader] and is responsible
 * for closing it. The returned sequence must be consumed before [reader]
 * is closed.
 *
 * @param reader Reader containing an OurAirports `airports.csv` file.
 * @return A lazy sequence of parsed [Airport] records.
 */
internal fun parseOurAirportsCsv(reader: Reader): Sequence<Airport> {
    val parser = CSVFormat.DEFAULT.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .get()
        .parse(reader)

    return parser.asSequence()
        .map(CSVRecord::ourAirportCSVToAirport)
}

/**
 * Converts an OurAirports CSV record to the common [Airport] representation.
 *
 * @return The airport represented by this CSV record.
 */
private fun CSVRecord.ourAirportCSVToAirport(): Airport =
    Airport(
        id = this["id"].toLong(),
        ident = this["ident"],
        type = this["type"],
        name = this["name"],
        latitudeDeg = this["latitude_deg"].toDouble(),
        longitudeDeg = this["longitude_deg"].toDouble(),
        elevationFt = this["elevation_ft"].toIntOrNull(),
        continent = this["continent"],
        isoCountry = this["iso_country"],
        isoRegion = this["iso_region"],
        municipality = this["municipality"].ifBlank { null },
        scheduledService = this["scheduled_service"] == HAS_SCHEDULED_SERVICE_MARKER,
        gpsCode = this["gps_code"].ifBlank { null },
        icaoCode = this["icao_code"].ifBlank { null },
        iataCode = this["iata_code"].ifBlank { null },
        localCode = this["local_code"].ifBlank { null },
        homeLink = this["home_link"].ifBlank { null },
        wikipediaLink = this["wikipedia_link"].ifBlank { null },
        keywords = this["keywords"].ifBlank { null },
    )

private const val HAS_SCHEDULED_SERVICE_MARKER = "yes"