package no.nav.tiltakspenger.person.pdl.models

import no.nav.tiltakspenger.person.Changeable
import no.nav.tiltakspenger.person.nyeste
import java.time.LocalDateTime

//interface Changeable {
//    val metadata: EndringsMetadata
//    val folkeregistermetadata: FolkeregisterMetadata?
//}

fun getEndringstidspunktOrNull(data: Changeable): LocalDateTime? =
    when {
        data.metadata.master.isFreg() -> data.folkeregistermetadata?.ajourholdstidspunkt
        else -> data.metadata.endringer.nyeste()?.registrert
    }
