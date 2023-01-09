package no.nav.tiltakspenger.person.pdl.models

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.nav.tiltakspenger.person.FREG
import no.nav.tiltakspenger.person.Fødsel
import no.nav.tiltakspenger.person.pdl.PDLClientError
import java.time.LocalDate

//data class Fødsel(
//    val foedselsdato: LocalDate,
//    override val folkeregistermetadata: FolkeregisterMetadata,
//    override val metadata: EndringsMetadata
//) : Changeable

//const val FREG = "FREG"
fun String.isFreg() = this.equals(FREG, ignoreCase = true)

fun avklarFødsel(foedsler: List<Fødsel>): Either<PDLClientError, Fødsel> {
    val foedslerSortert = foedsler.sortedByDescending { getEndringstidspunktOrNull(it) }
    val foedselFreg = foedslerSortert.find { it.metadata.master.isFreg() }
    return foedselFreg?.right() ?: foedslerSortert.firstOrNull()?.right()
    ?: PDLClientError.FødselKunneIkkeAvklares.left()
}
