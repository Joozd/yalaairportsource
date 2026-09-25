import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import nl.joozd.airportsource.yasinterfaces.AirportData
import java.nio.file.Path
import java.security.MessageDigest
import java.util.zip.GZIPInputStream
import kotlin.io.path.readBytes

/**
 * Reads GZIP-compressed JSON from [path] and deserializes it as [AirportData].
 *
 * The compressed file is read into memory before processing. If [sha256] is
 * provided, its SHA-256 digest is verified before decompression and
 * deserialization.
 *
 * @throws AirportDataIntegrityException if the calculated SHA-256 digest does not match [sha256].
 */
@OptIn(ExperimentalSerializationApi::class)
fun getAirportDataFromGzipJson(
    path: Path,
    sha256: String? = null,
): AirportData {
    val compressedData = path.readBytes()

    if (sha256 != null) {
        val calculatedSha256 = MessageDigest
            .getInstance("SHA-256")
            .digest(compressedData)
            .toHexString()

        if(!calculatedSha256.equals(sha256, ignoreCase = true)) {
            throw AirportDataIntegrityException("SHA-256 mismatch for $path: expected $sha256, got $calculatedSha256")
        }
    }

    return GZIPInputStream(compressedData.inputStream()).use {
        Json.decodeFromStream<AirportData>(it)
    }
}