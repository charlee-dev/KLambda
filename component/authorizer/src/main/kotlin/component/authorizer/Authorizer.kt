package component.authorizer

import com.nimbusds.jose.proc.JWSKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.peekFailure
import dev.forkhandles.result4k.resultFrom
import dev.forkhandles.result4k.valueOrNull
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Clock
import java.util.Date

private class DeterministicJwtClaimSetVerifier(
    private val clock: Clock,
    exactMatchClaims: JWTClaimsSet = JWTClaimsSet.Builder().build(),
    requiredClaims: Set<String> = emptySet(),
) : DefaultJWTClaimsVerifier<SecurityContext>(exactMatchClaims, requiredClaims) {
    override fun currentTime(): Date = Date.from(clock.instant())
}

fun interface Authorizer : (String) -> EmailHash?

fun jwtAuthorizer(
    audience: List<String>,
    clock: Clock,
    issuer: String,
    keySelector: JWSKeySelector<SecurityContext>
): Authorizer {
    val claims = JWTClaimsSet.Builder()
        .issuer(issuer)
        .audience(audience)
        .build()
    val verifier = DeterministicJwtClaimSetVerifier(
        exactMatchClaims = claims,
        requiredClaims = setOf("email"),
        clock = clock
    )
    val processor = DefaultJWTProcessor<SecurityContext>().apply {
        jwtClaimsSetVerifier = verifier
        jwsKeySelector = keySelector
    }

    return Authorizer { token ->
        resultFrom { SignedJWT.parse(token).let { processor.process(it, null) } }
            .peekFailure {
                val logger = KotlinLogging.logger {}
                logger.error { "Failed to process JWT: $it" }
            }
            .map { claims ->
                val email = claims.claims.getValue("email").toString()
                EmailHash.fromEmail(email)
            }
            .valueOrNull()
    }
}
