package lab.arahnik.authentication.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.lang.Function;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    private final static String JWT_SECRET = "d9a387d0dfbb45fe579126b0118ba2643eebe6b25f41ec0703f6670c11b663aa";

    public boolean isTokenValid(String token, UserDetails details) {
        final String username = extractUsername(token);
        return username.equals(
                details.getUsername()
        ) && !isTokenExpired(token);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(
                new HashMap<>(),
                userDetails
        );
    }

    public String generateToken(
        Map<String, Object> claims,
        UserDetails details
    ) {
        return Jwts
                .builder()
                .claims(claims)
                .subject(details.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 100))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return getExpirationDate(token).before(new Date());
    }

    private Date getExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private SecretKey getSigningKey() {
        byte[] bytes = Decoders.BASE64.decode(JWT_SECRET);
        return Keys.hmacShaKeyFor(bytes);
    }
}
