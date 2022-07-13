package web.cloudfilestorage.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import web.cloudfilestorage.exceptions.JwtAuthenticationException;
import web.cloudfilestorage.model.Role;
import web.cloudfilestorage.service.UserService;

import javax.annotation.PostConstruct;
;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${jwt.token.secret}")
    private String secret;

    private Key key;

    @Value("${jwt.token.expired}")
    private long validityInMilliseconds;

    private final UserService userService;

    @Autowired
    public JwtTokenProvider(UserService userService) {
        this.userService = userService;
    }

    @PostConstruct
    protected void init() {
        secret = Base64.getEncoder().encodeToString(secret.getBytes());
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // public String createToken(String username, List<Role> roles) {
    public String createToken(String username, Collection<? extends GrantedAuthority> roles)
        throws
            JwtException
    {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(
                    key,
                    SignatureAlgorithm.HS256
                )
                .compact();
    }

    public Authentication getAuthentication(String token) throws UsernameNotFoundException {
        UserDetails user = userService.loadUserByUsername(getUsername(token));
        return new UsernamePasswordAuthenticationToken(
                user, "", user.getAuthorities()
        );
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    public String resolveToken(HttpServletRequest request) {
        String bearer_token = request.getHeader("Authorization");
        if (bearer_token != null && bearer_token.startsWith("Bearer_")) {
            return bearer_token.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token) throws JwtAuthenticationException {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key).build()
                    .parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtAuthenticationException(
                    "Jwt token is expired or invalid!",
                    "Authorization"
            );
        }
    }

    private List<String> getRoleNames(List<Role> roles) {
        return roles.stream().map(Role::getName).collect(Collectors.toList());
    }

}
