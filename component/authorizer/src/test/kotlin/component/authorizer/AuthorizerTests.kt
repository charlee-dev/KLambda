package component.authorizer

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.proc.JWSKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.security.KeyPairGenerator
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Date

class JwtAuthorizerTest {
    private val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
    private val issuer = "test_issuer"
    private val audience = listOf("test_audience")
    private val keySelector = mockk<JWSKeySelector<SecurityContext>>()
    private val emailStub = "test@example.com"
    private val mockSignedJWT = mockk<SignedJWT>(relaxed = true)
    private val mockJWSHeader = mockk<JWSHeader>(relaxed = true)
    private val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
    private val mockJCAKey = keyPairGenerator.generateKeyPair().public

    @Test
    fun `jwtAuthorizer returns null when token is invalid`() {
        every { keySelector.selectJWSKeys(any(), any()) } returns emptyList()
        val authorizer = jwtAuthorizer(audience, clock, issuer, keySelector)
        val result = authorizer("invalid_token")
        assertNull(result)
    }

    @Test
    fun `jwtAuthorizer returns null when token does not contain email claim`() {
        every { keySelector.selectJWSKeys(any(), any()) } returns emptyList()
        val authorizer = jwtAuthorizer(audience, clock, issuer, keySelector)
        val result = authorizer("token_without_email")
        assertNull(result)
    }

    @Test
    fun `jwtAuthorizer returns null when token cannot be parsed`() {
        every { keySelector.selectJWSKeys(any(), any()) } returns emptyList()
        val authorizer = jwtAuthorizer(audience, clock, issuer, keySelector)
        val result = authorizer("unparseable_token")
        assertNull(result)
    }

    @Test
    fun `jwtAuthorizer parses valid token and returns valid EmailHash`() {
        val jwtClaimsSet = JWTClaimsSet.Builder()
            .subject(emailStub)
            .audience(audience)
            .issuer(issuer)
            .claim("email", emailStub)
            .build()

        mockkStatic(SignedJWT::class)
        every { SignedJWT.parse("valid_token") } returns mockSignedJWT
        every { mockSignedJWT.jwtClaimsSet } returns jwtClaimsSet
        every { mockSignedJWT.header } returns mockJWSHeader
        every { mockJWSHeader.type } returns JOSEObjectType.JWT
        every { mockJWSHeader.algorithm } returns JWSAlgorithm.RS256
        every { keySelector.selectJWSKeys(any(), any()) } returns listOf(mockJCAKey)
        every { mockSignedJWT.verify(any()) } returns true

        val authorizer = jwtAuthorizer(audience, clock, issuer, keySelector)
        val result = authorizer("valid_token")

        assertEquals(EmailHash.fromEmail(emailStub), result)

        unmockkStatic(SignedJWT::class)
    }

    @Test
    fun `jwtAuthorizer rejects expired token`() {
        val expiredJWTClaimsSet = JWTClaimsSet.Builder()
            .subject(emailStub)
            .audience(audience)
            .issuer(issuer)
            .expirationTime(Date.from(Instant.EPOCH)) // Expired at the start of UNIX time
            .build()

        every { mockSignedJWT.jwtClaimsSet } returns expiredJWTClaimsSet

        val authorizer = jwtAuthorizer(audience, clock, issuer, keySelector)

        val result = authorizer("expired_token")

        assertNull(result)
        unmockkStatic(SignedJWT::class)
    }

    @Test
    fun `jwtAuthorizer rejects token with wrong issuer`() {
        val wrongIssuerJWTClaimsSet = JWTClaimsSet.Builder()
            .subject(emailStub)
            .audience(audience)
            .issuer("wrong_issuer")
            .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
            .build()

        every { mockSignedJWT.jwtClaimsSet } returns wrongIssuerJWTClaimsSet

        val authorizer = jwtAuthorizer(audience, clock, issuer, keySelector)

        val result = authorizer("wrong_issuer_token")

        assertNull(result)
    }
}
