package no.nav.tiltakspenger.fakta.person.pdl.models

data class Person(
    val fødsel: Fødsel?,
    val navn: Navn
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "fødselsdato" to this.fødsel?.foedselsdato,
            "fornavn" to this.navn.fornavn,
            "etternavn" to this.navn.etternavn,
            "mellomnavn" to this.navn.mellomnavn,
        )
    }
}