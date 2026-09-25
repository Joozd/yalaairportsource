package nl.joozd.airportsource.io

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.nio.file.Path
import java.util.zip.GZIPInputStream
import kotlin.io.path.inputStream

/**
 * Reads GZIP-compressed JSON from [path] and deserializes it as [T].
 */
@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> getSerializableFromGzipJson(path: Path): T =
    GZIPInputStream(path.inputStream()).use { input ->
        Json.decodeFromStream<T>(input)
    }