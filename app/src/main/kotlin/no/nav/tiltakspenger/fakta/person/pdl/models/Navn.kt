package no.nav.tiltakspenger.fakta.person.pdl.models;

import kotlinx.serialization.Serializable;
import no.nav.tiltakspenger.fakta.person.pdl.EndringsMetadata

@Serializable
data class Navn(
    val fornavn: String,
    val etternavn: String,
    val mellomnavn: String?,
    val metadata: EndringsMetadata,
    val folkeregistermetadata: FolkeregisterMetadata
)
