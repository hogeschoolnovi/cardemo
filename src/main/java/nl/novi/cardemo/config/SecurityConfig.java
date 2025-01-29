package nl.novi.cardemo.config;

import nl.novi.cardemo.services.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // Publieke endpoints
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/cars").hasAnyRole("USER")

                        // Beveiligde endpoints: Alleen voor admins
                        .requestMatchers(HttpMethod.POST, "/cars/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/cars/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/cars/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/cars/{carId}/carregistrations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/cars/{carId}/carregistrations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/cars/{carId}/carregistrations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/cars/{carId}/carregistrations/**").hasRole("ADMIN")

                        // Andere verzoeken worden geweigerd
                        .anyRequest().denyAll()
                )
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        ;
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        var builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());

        return builder.build();
    }
}