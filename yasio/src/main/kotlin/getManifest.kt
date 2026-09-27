import kotlinx.serialization.json.Json
import nl.joozd.airportsource.yasinterfaces.AirportDataManifest
import java.net.URI

fun getManifest(downloadUriPrefix: String): AirportDataManifest {
    val manifestText: String = URI(downloadUriPrefix + AirportDataManifest.MANIFEST_FILE_NAME)
        .toURL()
        .readText()

    return Json.decodeFromString<AirportDataManifest>(manifestText)
}