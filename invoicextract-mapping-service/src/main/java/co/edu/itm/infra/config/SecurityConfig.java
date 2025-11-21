package co.edu.itm.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> {})
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Swagger and health endpoints
                .requestMatchers(
                        new AntPathRequestMatcher("/v3/api-docs/**"),
                        new AntPathRequestMatcher("/swagger-ui/**"),
                        new AntPathRequestMatcher("/swagger-ui.html"),
                        new AntPathRequestMatcher("/actuator/health**")
                ).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Business routes
                .requestMatchers("/api/configs/**").hasRole("ADMIN")
                .requestMatchers("/api/invoices/**").hasAnyRole("FINANZAS", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/erps/**").hasAnyRole("FINANZAS", "TECNICO", "ADMIN")
                .requestMatchers("/api/erps/**", "/api/mappings/**", "/api/reference/**").hasAnyRole("TECNICO", "ADMIN")
                .requestMatchers("/api/export/**").hasAnyRole("FINANZAS", "ADMIN")

                // All remaining endpoints require authentication
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(keycloakRealmRoleConverter())
                )
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        
        // Skip issuer validation to allow tokens from localhost and host.docker.internal
        // This is necessary for CI/CD where tests run on host but backend runs in Docker
        OAuth2TokenValidator<Jwt> validator = JwtValidators.createDefault();
        jwtDecoder.setJwtValidator(validator);
        
        return jwtDecoder;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:3001",
                "http://127.0.0.1:3001"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public JwtAuthenticationConverter keycloakRealmRoleConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthoritiesFromKeycloak);
        return converter;
    }

    private Collection<GrantedAuthority> extractAuthoritiesFromKeycloak(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        List<String> realmRoles = List.of();
        if (realmAccess != null) {
            Object roles = realmAccess.get("roles");
            if (roles instanceof List<?> list) {
                realmRoles = list.stream().filter(Objects::nonNull).map(Object::toString).toList();
            }
        }

        // Include client roles from resource_access.*.roles (avoid pattern matching with generics for Java 17)
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        Stream<String> clientRoles = Stream.empty();
        if (resourceAccess != null) {
            clientRoles = resourceAccess.values().stream()
                    .filter(v -> v instanceof Map)
                    .map(v -> (Map<?, ?>) v)
                    .map(m -> m.get("roles"))
                    .filter(v -> v instanceof List)
                    .flatMap(v -> ((List<?>) v).stream())
                    .filter(Objects::nonNull)
                    .map(Object::toString);
        }

        return Stream.concat(
                    Stream.concat(realmRoles.stream(), clientRoles)
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .map(SimpleGrantedAuthority::new),
                    Stream.<GrantedAuthority>empty()
               )
               .collect(Collectors.toSet());
    }
}
