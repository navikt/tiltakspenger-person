package no.nav.tiltakspenger.fakta.person.pdl.models

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.fakta.person.pdl.PDLClientError
import no.nav.tiltakspenger.fakta.person.serializers.LocalDateSerializer
import java.time.LocalDate

@Serializable
data class Fødsel(
    @Serializable(with = LocalDateSerializer::class)
    val foedselsdato: LocalDate,
    override val folkeregistermetadata: FolkeregisterMetadata,
    override val metadata: EndringsMetadata
) : Changeable

const val FREG = "FREG"
fun String.isFreg() = this.equals(FREG, ignoreCase = true)

fun avklarFødsel(foedsler: List<Fødsel>): Either<PDLClientError, Fødsel> {
    val foedslerSortert = foedsler.sortedByDescending { getEndringstidspunktOrNull(it) }
    val foedselFreg = foedslerSortert.find { it.metadata.master.isFreg() }
    return foedselFreg?.right() ?: foedslerSortert.firstOrNull()?.right()
    ?: PDLClientError.FødselKunneIkkeAvklares.left()
}
