package nl.joozd.yalaairportsource

import nl.joozd.airportsource.yasinterfaces.AirportDataDelta
import nl.joozd.airportsource.yasinterfaces.FullAirportData

sealed interface DownloadResult {
    @JvmInline
    value class Full(val airportData: FullAirportData) : DownloadResult

    @JvmInline
    value class Incremental(val airportData: AirportDataDelta) : DownloadResult

    data object UpToDate : DownloadResult
}