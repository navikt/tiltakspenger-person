package no.nav.tiltakspenger.fakta.person.pdl.models

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.fakta.person.seralizers.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class FolkeregisterMetadata(
    val aarsak: String?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val ajourholdstidspunkt: LocalDateTime?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val gyldighetstidspunkt: LocalDateTime?,
    val kilde: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val opphoerstidspunkt: LocalDateTime?,
    val sekvens: Int?
)
