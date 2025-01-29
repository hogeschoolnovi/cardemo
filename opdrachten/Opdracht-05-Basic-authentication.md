
# Beveiliging

## **Stap 1: Maak een User Entity**

Maak een User-entity om gebruikers in de database op te slaan.

**Bestand:** `src/main/java/nl/novi/cardemo/models/User.java`

```java
package nl.novi.cardemo.models;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    // Getters en setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
```

---

## **Stap 2: Maak een UserRepository**

Voeg een repository toe om gebruikers uit de database te laden.

**Bestand:** `src/main/java/nl/novi/cardemo/repositories/UserRepository.java`

```java
package nl.novi.cardemo.repositories;

import nl.novi.cardemo.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

---

## **Stap 3: Implementeer een UserDetailsService**

Maak een service die Spring Security kan gebruiken om gebruikers te laden.

**Bestand:** `src/main/java/nl/novi/cardemo/services/UserDetailsServiceImpl.java`

```java
package nl.novi.cardemo.services;

import nl.novi.cardemo.models.User;
import nl.novi.cardemo.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Gebruiker niet gevonden: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
}
```

Voeg ook de juiste dependecies toe
````xml
 <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
````

---

## **Stap 4: Voeg een beveiligingsconfiguratie toe**

Pas Spring Security aan om toegangsbeheer en authenticatie te configureren.

**Bestand:** `src/main/java/nl/novi/cardemo/config/SecurityConfig.java`

```java
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
```

---

## **Stap 5: Voeg gebruikers toe aan data.sql**

Om gebruikers toe te voegen, gebruik je `data.sql`. Versleutel je wachtwoord met het volgende programma:

**Wachtwoord Versleutelen:**

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "jouw-wachtwoord";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println(encodedPassword);
    }
}
```

Gebruik de uitvoer om het geëncodeerde wachtwoord toe te voegen in `data.sql`:

```sql
INSERT INTO users (username, password, role) VALUES ('admin', '$2a$10$4tU5m...', 'ADMIN');
INSERT INTO users (username, password, role) VALUES ('user', '$2a$10$...', 'USER');
```

---

## **Stap 6: Testen met Postman**

### **Publieke toegang testen**
1. Stuur een `GET`-verzoek naar `/api/cars`.
2. Je hebt geen authenticatie nodig.

### **Inloggen met Basic Auth**
1. Kies in Postman onder `Authorization` het type `Basic Auth`.
2. Voer je gebruikersnaam en wachtwoord in.

### **Acties testen**
1. **Registratie toevoegen:** Stuur een `POST`-verzoek naar `/api/carregistrations` met een JSON-body:
   ```json
   {
     "carId": 1,
     "owner": "John Doe",
     "registrationDate": "2023-01-01"
   }
   ```
2. **Registratie verwijderen (admin):** Stuur een `DELETE`-verzoek naar `/api/carregistrations/{id}` met admin-credentials.

### **Foutmeldingen**
Test zonder in te loggen of met verkeerde gegevens en controleer de `401 Unauthorized` foutmeldingen.
