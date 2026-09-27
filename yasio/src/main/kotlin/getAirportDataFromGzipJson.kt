import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import nl.joozd.airportsource.yasinterfaces.AirportData
import nl.joozd.airportsource.yasinterfaces.AirportDataFile
import java.net.URI
import java.nio.file.Path
import java.security.MessageDigest
import java.util.zip.GZIPInputStream

/**
 * Reads GZIP-compressed JSON from [uri] and deserializes it as [AirportData].
 *
 * The compressed file is read into memory before processing. If [sha256] is
 * provided, its SHA-256 digest is verified before decompression and
 * deserialization.
 *
 * @throws AirportDataIntegrityException if the calculated SHA-256 digest does not match [sha256].
 */
@OptIn(ExperimentalSerializationApi::class)
fun getAirportDataFromGzipJson(
    uri: URI,
    sha256: String? = null,
): AirportData {
    val compressedData = uri.toURL().readBytes()

    if (sha256 != null) {
        val calculatedSha256 = MessageDigest
            .getInstance("SHA-256")
            .digest(compressedData)
            .toHexString()

        if(!calculatedSha256.equals(sha256, ignoreCase = true)) {
            throw AirportDataIntegrityException("SHA-256 mismatch for $uri: expected $sha256, got $calculatedSha256")
        }
    }

    return GZIPInputStream(compressedData.inputStream()).use {
        Json.decodeFromStream<AirportData>(it)
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun getAirportDataFromGzipJson(
    path: Path,
    sha256: String? = null,
) = getAirportDataFromGzipJson(path.toUri(), sha256)




fun getAirportData(downloadUriPrefix: String, airportDataFile: AirportDataFile): AirportData {
    val uri = URI(downloadUriPrefix + airportDataFile.filename)
    val sha256 = airportDataFile.sha256

    return getAirportDataFromGzipJson(uri, sha256)
}