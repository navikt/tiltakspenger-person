package no.nav.tiltakspenger.fakta.person.pdl.models

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.fakta.person.pdl.EndringsMetadata
import no.nav.tiltakspenger.fakta.person.seralizers.LocalDateSerializer
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

fun avklarFødsel(foedsler: List<Fødsel>): Fødsel? {
    val foedslerSortert = foedsler.sortedByDescending { getEndringstidspunktOrNull(it) }
    val foedselFreg = foedslerSortert.find { it.metadata.master.isFreg() }
    return foedselFreg ?: foedslerSortert.firstOrNull()
}
