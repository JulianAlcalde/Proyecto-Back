package ar.edu.utn.tpi.shared.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class KeycloakRolesConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        Object value = realmAccess == null ? List.of() : realmAccess.getOrDefault("roles", List.of());
        Stream<GrantedAuthority> roleAuthorities = value instanceof Collection<?> roles
                ? roles.stream().map(Object::toString).map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                : Stream.empty();
        Collection<GrantedAuthority> authorities = Stream.concat(
                scopeConverter.convert(jwt).stream(),
                roleAuthorities
        ).toList();
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }
}
