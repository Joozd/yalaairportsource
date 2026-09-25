package nl.joozd.airportsource.ourairports

import nl.joozd.airportsource.yasinterfaces.FullAirportData
import java.net.URI
import java.time.LocalDate

/**
 * Downloads and parses airport data from the OurAirports CSV dataset.
 *
 * @param downloadURI URI from which the airport CSV data is downloaded.
 */
class OurAirportsDownloader(downloadURI: URI) {
    private val downloadURL = downloadURI.toURL()

    /**
     * Downloads the complete airport dataset and associates it with the given date.
     *
     * The date is stored as its epoch-day value in the resulting [FullAirportData].
     *
     * @param date date to associate with the downloaded dataset. Defaults to the current date.
     * @return the parsed airport dataset and its associated epoch day.
     */
    fun getFullAirportData(date: LocalDate = LocalDate.now()): FullAirportData {
        val epochDay = date.toEpochDay()
        return downloadURL.openStream().bufferedReader().use { reader ->
            FullAirportData(
                parseOurAirportsCsv(reader).toList(),
                epochDay
            )
        }
    }
}