package ar.edu.utn.tpi.gateway;

import ar.edu.utn.tpi.shared.security.KeycloakRolesConverter;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration @EnableWebFluxSecurity
public class SecurityConfig {
    @Bean SecurityWebFilterChain security(ServerHttpSecurity http) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(auth -> auth.pathMatchers("/actuator/health", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/technician-docs/**", "/work-order-docs/**", "/webjars/**").permitAll() //Agregué "/webjars/**"
                .pathMatchers("/api/technicians/**").hasAnyRole("DISPATCHER", "ADMIN").anyExchange().authenticated()) 
            .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(new ReactiveJwtAuthenticationConverterAdapter(new KeycloakRolesConverter())))).build();
    }
}
