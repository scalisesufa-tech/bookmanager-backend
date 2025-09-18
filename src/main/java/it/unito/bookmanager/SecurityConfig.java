package it.unito.bookmanager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Rende il token disponibile come request attribute (compat con Spring Security 6)
        var csrfReqHandler = new CsrfTokenRequestAttributeHandler();
        csrfReqHandler.setCsrfRequestAttributeName("_csrf");

        http
            .authorizeHttpRequests(auth -> auth
                // consenti tutte le OPTIONS (preflight CORS)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                .requestMatchers(
                    "/error",
                    "/login**",
                    "/oauth2/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/webjars/**"
                ).permitAll()

                // Swagger visibile solo dopo login
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").authenticated()

                // il resto (incluso "/") richiede login
                .anyRequest().authenticated()
            )
            .cors(Customizer.withDefaults())
            // CSRF con cookie leggibile e header X-XSRF-TOKEN (default del CookieCsrfTokenRepository)
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(csrfReqHandler)
            )
            .oauth2Login(oauth -> oauth
                .loginPage("/oauth2/authorization/google")
                .defaultSuccessUrl("/", true) // torna alla tua app
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            )
            // forza l’emissione del cookie XSRF-TOKEN al primo GET (utile per Swagger)
            .addFilterAfter(csrfCookieFilter(), org.springframework.security.web.csrf.CsrfFilter.class);

        return http.build();
    }

    @Bean
    OncePerRequestFilter csrfCookieFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                // Accedere al token è sufficiente: il CookieCsrfTokenRepository gestisce il cookie
                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
            "http://localhost:3000",
            "http://localhost:3001",
            "http://localhost:8080",
            "https://*.a.run.app"
        ));
        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type","Authorization","X-CSRF-TOKEN","X-XSRF-TOKEN"));
        config.setExposedHeaders(List.of("Location")); // utile per leggere Location nei 201

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
