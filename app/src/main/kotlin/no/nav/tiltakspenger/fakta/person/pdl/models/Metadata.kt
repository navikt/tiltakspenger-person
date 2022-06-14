package no.nav.tiltakspenger.fakta.person.pdl

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.fakta.person.seralizers.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Endring(
    val kilde: String,
    // Should use date-serializer
    @Serializable(with = LocalDateTimeSerializer::class)
    val registrert: LocalDateTime?,
    val registrertAv: String,
    val systemkilde: String,
    val type: String
)

fun List<Endring>.nyeste(): Endring? = this
    .filter { it.registrert != null }
    .maxByOrNull { it.registrert!! }

@Serializable
data class EndringsMetadata(
    val endringer: List<Endring>,
    val master: String
)
