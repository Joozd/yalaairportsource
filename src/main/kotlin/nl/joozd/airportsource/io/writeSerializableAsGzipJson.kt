package nl.joozd.airportsource.io

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.nio.file.Path
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.util.zip.GZIPOutputStream
import kotlin.io.path.fileSize
import kotlin.io.path.outputStream

/**
 * Serializes [serializable] as JSON, compresses it with GZIP, and writes it to [target].
 *
 * @return Metadata describing the compressed file that was written.
 */
@ExperimentalSerializationApi
inline fun <reified T> writeSerializableAsGzipJson(
    serializable: T,
    target: Path,
): WriteResult {
    val digest = MessageDigest.getInstance("SHA-256")

    DigestOutputStream(target.outputStream(), digest).use { digestOutput ->
        GZIPOutputStream(digestOutput).use { gzipOutput ->
            Json.encodeToStream(serializable, gzipOutput)
        }
    }

    return WriteResult(
        sha256 = digest.digest().toHexString(),
        size = target.fileSize(),
    )
}