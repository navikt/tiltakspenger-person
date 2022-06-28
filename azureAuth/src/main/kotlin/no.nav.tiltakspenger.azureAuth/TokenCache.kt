package no.nav.tiltakspenger.azureAuth

import java.time.LocalDateTime

const val LEEWAY_SECONDS: Long = 60
class TokenCache {
    var token: String? = null
        private set
    private var expires: LocalDateTime? = null

    fun isExpired(): Boolean = expires
        ?.isBefore(LocalDateTime.now().plusSeconds(LEEWAY_SECONDS)) ?: true

    fun update(accessToken: String, expiresIn: Long) {
        token = accessToken
        expires = LocalDateTime.now().plusSeconds(expiresIn)
    }
}
