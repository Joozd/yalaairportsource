package nl.joozd.airportsource.yasinterfaces

import kotlinx.serialization.Serializable

/**
 * Airport data as supplied by an airport data source.
 *
 * The fields correspond to the fields provided by the OurAirports
 * airports dataset. Optional source fields are represented by nullable
 * properties.
 */
@Serializable
data class Airport(
    val id: Long,
    val ident: String,
    val type: String,
    val name: String,
    val latitudeDeg: Double,
    val longitudeDeg: Double,
    val elevationFt: Int?,
    val continent: String,
    val isoCountry: String,
    val isoRegion: String,
    val municipality: String?,
    val scheduledService: Boolean,
    val gpsCode: String?,
    val icaoCode: String?,
    val iataCode: String?,
    val localCode: String?,
    val homeLink: String?,
    val wikipediaLink: String?,
    val keywords: String?,
)