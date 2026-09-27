import nl.joozd.yalaairportsource.DownloadResult
import nl.joozd.yalaairportsource.YasDownloader
import org.junit.jupiter.api.Test

/**
 * Integration test
 */
class DownloadTest {
    @Test
    fun `test if we can get stuff`(){
        val downloader = YasDownloader()
        val full = downloader.getFullAirportData()
        assert(full is DownloadResult.Full)
        full as DownloadResult.Full
        val currentVersion = full.airportData.version
        val twoEarlier = currentVersion-2
        val delta = downloader.getUpdatesFromVersion(twoEarlier)
        assert(delta is DownloadResult.Incremental)
        delta as DownloadResult.Incremental
        println(delta)
    }
}