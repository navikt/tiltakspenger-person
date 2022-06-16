// package no.nav.tiltakspenger.fakta.person.pdl.models
//
// import arrow.core.Either
// import arrow.core.left
// import arrow.core.right
// import kotlinx.serialization.Serializable
// import no.nav.tiltakspenger.fakta.person.pdl.EndringsMetadata
// import no.nav.tiltakspenger.fakta.person.pdl.PDLClientError
//
// @Serializable
// data class Gradering(
//     val gradering: AdressebeskyttelseGradering,
//     override val metadata: EndringsMetadata,
//     override val folkeregistermetadata: FolkeregisterMetadata
// ) : Changeable
//
// fun avklarGradering(gradering: List<Gradering>): Either<PDLClientError, Gradering> {
//     if (gradering.isEmpty()) return PDLClientError.IngenGraderingFunnet.left()
//     return gradering
//         .sortedByDescending { getEndringstidspunktOrNull(it) }
//         .firstOrNull { !kildeErUdokumentert(it.metadata) }?.right()
//         ?: PDLClientError.GraderingKunneIkkeAvklares.left()
// }
