package no.nav.tiltakspenger.fakta.person.pdl.models

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.fakta.person.pdl.EndringsMetadata
import no.nav.tiltakspenger.fakta.person.pdl.nyeste
import java.time.LocalDateTime

@Serializable
data class Fødsel(
    val foedselsdato: String,
    val folkeregistermetadata: FolkeregisterMetadata,
    val metadata: EndringsMetadata
)

const val FREG = "FREG"
fun String.isFreg() = this.equals(FREG, ignoreCase = true)

fun avklarFødsel(foedsler: List<Fødsel>): Fødsel? {
    val foedslerSortert = foedsler.sortedByDescending { getEndringstidspunktOrNull(it) }
    val foedselFreg = foedslerSortert.find { it.metadata.master.isFreg() }
    return foedselFreg ?: foedslerSortert.firstOrNull()
}

fun getEndringstidspunktOrNull(fødsel: Fødsel): LocalDateTime? =
    when {
        fødsel.metadata.master.isFreg() -> fødsel.folkeregistermetadata.ajourholdstidspunkt
        else -> fødsel.metadata.endringer.nyeste()?.registrert
    }
