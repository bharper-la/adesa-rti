package com.la.dataservices.adesa_rti;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableConfigurationProperties(AuthProps.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthFilter authFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var h2 = PathRequest.toH2Console();

        return http
                // H2 console uses frames; allow SAMEORIGIN
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                // If you keep CSRF off globally, this is fine. Otherwise:
                // .csrf(csrf -> csrf.ignoringRequestMatchers(h2))  // ignore CSRF just for H2
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(reg -> reg
                        // Permit H2 console completely
                        .requestMatchers(h2).permitAll()
                        // Fallback for custom H2 paths (some setups use /h2/**)
                        .requestMatchers("/h2/**", "/h2-console/**").permitAll()

                        // Your existing controller rules
                        .requestMatchers(HttpMethod.OPTIONS, "/events", "/events/**").permitAll()
                        .requestMatchers("/events/**").permitAll()

                        //ping endpoint
                        .requestMatchers("/ping").permitAll()

                        // Everything else (adjust as needed)
                        .anyRequest().permitAll()
                )
                // Your header auth filter stays in the chain
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
