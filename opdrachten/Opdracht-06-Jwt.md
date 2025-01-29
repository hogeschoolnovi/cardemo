# Authenticatie van Basic naar JWT

## Stap 1: Toevoegen van benodigde dependencies

Om JWT-functionaliteit te ondersteunen in de applicatie, moeten enkele dependencies worden toegevoegd aan het project.
Deze dependencies zijn nodig voor het genereren en valideren van JWT-tokens.

### **Context:**

De `io.jsonwebtoken` library biedt alle benodigde tools voor het werken met JWT-tokens. Je voegt deze dependencies toe
aan het `pom.xml`-bestand als je een Maven-project gebruikt.

### **Implementatie:**

Open het bestand `pom.xml` in de root van je project en voeg de volgende dependencies toe binnen de `<dependencies>`
-tag:

<details>
<summary>Klik hier om de code te bekijken</summary>

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
   <groupId>io.jsonwebtoken</groupId>
   <artifactId>jjwt-impl</artifactId>
   <version>0.11.5</version>
</dependency>
<dependency>
   <groupId>io.jsonwebtoken</groupId>
   <artifactId>jjwt-jackson</artifactId>
   <version>0.11.5</version>
</dependency>
```

</details>

### **Waarom deze dependencies?**

1. **`jjwt-api`:** Bevat de interfaces en abstracties voor het werken met JWT-tokens.
2. **`jjwt-impl`:** De implementatie van de `jjwt-api` functies.
3. **`jjwt-orgjson`:** Ondersteunt JSON-objecten binnen JWT-tokens.

Na het toevoegen van deze dependencies, synchroniseer je het project met Maven om de libraries te downloaden en
beschikbaar te maken binnen het project.

---

## Stap 2: Toevoegen van nieuwe bestanden

Bij JWT-authenticatie zijn enkele nieuwe klassen nodig. Implementeer deze volgens de instructies.

### Configureren van JWT-instellingen uit `application.properties`**

Om gevoelige gegevens zoals de geheime sleutel en het publiek voor JWT-tokens te beheren, halen we deze waarden uit het
bestand `application.properties`. Dit maakt het eenvoudiger om instellingen aan te passen zonder dat de broncode opnieuw
moet worden opgebouwd.

#### **Context:**

Het gebruik van de `application.properties` of `application.yml` voor configuratie is een goede praktijk in Spring
Boot-projecten. Hiermee wordt de configuratie losgekoppeld van de applicatielogica.

#### **Implementatie:**

1. **Toevoegen van configuratie in `application.properties`**

   Open het bestand `src/main/resources/application.properties` en zorg dat de volgende regels aanwezig zijn:

   ```properties
   # JWT (aanpassing)
   jwt.SecretKey = eengeheimesleuteldieniemandmagwetenenhijmoetheelerglangencomplexzijnomtevoldoenaanallenormenenwaardeninjavaland
   jwt.Audience = cardemo-api.com
   ```

2. **Gebruik van de `@Value`-annotatie**

   In de klassen waar JWT-logica wordt gebruikt (zoals `JwtService`), haal je de waarden rechtstreeks op met de `@Value`
   -annotatie:

<details>
<summary>Klik hier om een voorbeeld te bekijken</summary>

```java

    @Value("${jwt.SecretKey}")
    private String SECRET_KEY;

    @Value("${jwt.Audience}")
    private String AUDIENCE;

  
```

</details>

#### **Waarom deze aanpak?**

- **Beveiliging:** Gevoelige gegevens worden niet hardcoded in de broncode.
- **Eenvoud:** De `@Value`-annotatie maakt het ophalen van configuratiewaarden direct en simpel.
- **Flexibiliteit:** Instellingen kunnen eenvoudig worden aangepast zonder de applicatie opnieuw te bouwen.

---

### JwtService.java

**Context:**
De `JwtService` is verantwoordelijk voor het genereren, valideren, en extraheren van informatie uit JWT-tokens. Het
bevat methoden zoals:

- `generateToken()`
- `validateToken()`
- `extractUsername()`

**Implementatie:**
<details>
<summary>Klik hier om de code te bekijken</summary>

```java
package nl.novi.cardemo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {
    @Value("${jwt.SecretKey}")
    private String SECRET_KEY;

    @Value("${jwt.Audience}")
    private String AUDIENCE;

    private String ROLES_CLAIMS_NAME = "roles";

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractAudience(String token) {
        return extractClaim(token, Claims::getAudience);
    }

    public List<GrantedAuthority> extractRoles(String token) {
        final Claims claims = extractAllClaims(token);
        List<String> roles = claims.get(ROLES_CLAIMS_NAME, List.class);
        if (roles == null) return Collections.emptyList(); // Geen rollen gevonden, retourneer lege lijst
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T>
            claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, 1000 * 60 * 60 * 24 * 10L); //tien dagen in ms
    }

    public String generateToken(UserDetails userDetails, Long milliSeconds) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put(ROLES_CLAIMS_NAME, roles);
        return createToken(claims, userDetails.getUsername(), milliSeconds); //time in milliseconds
    }

    private String createToken(Map<String, Object> claims, String
            subject, long milliSeconds) {

        long currentTime = System.currentTimeMillis();
        return createToken(claims, subject, currentTime, milliSeconds);
    }

    private String createToken(Map<String, Object> claims, String subject, long currentTime, long validPeriod) {
        return Jwts.builder()
                .setClaims(claims)
                .setAudience(AUDIENCE)
                .setSubject(subject)
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(currentTime + validPeriod))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token) {
        try {
            String tokenAudience = extractAudience(token);
            boolean isAudienceValid = tokenAudience.equals(AUDIENCE);
            return !isTokenExpired(token) && isAudienceValid;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

</details>

---

### JwtRequestFilter.java

**Context:**
Dit filter onderschept elke inkomend verzoek, valideert de JWT, en koppelt de gebruiker aan de context
met `SecurityContextHolder`.

**Implementatie:**
<details>
<summary>Klik hier om de code te bekijken</summary>

```java
package nl.novi.cardemo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {


    private final JwtService jwtService;

    public JwtRequestFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest
                                            request,
                                    @NonNull HttpServletResponse
                                            response,
                                    @NonNull FilterChain
                                            filterChain) throws ServletException, IOException {
        final String authorizationHeader =
                request.getHeader("Authorization");
        String username = null;
        List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
        String jwt = null;
        if (authorizationHeader != null &&
                authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtService.extractUsername(jwt);
            roles = jwtService.extractRoles(jwt);
        }
        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtService.validateToken(jwt)) {
                var usernamePasswordAuthenticationToken = new
                        UsernamePasswordAuthenticationToken(
                        username, null,
                        roles
                );
                usernamePasswordAuthenticationToken.setDetails(new
                        WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}

```

</details>

---

### Aanmaken van een DTO voor gebruikers inlog gegevens

Om gebruikersinloggegevens gestructureerd te ontvangen in de applicatie, maken we een **Data Transfer Object (DTO)**
aan. Dit DTO wordt gebruikt om de gebruikersnaam en het wachtwoord van de gebruiker over te dragen via de API.

#### **Context:**

Een DTO (Data Transfer Object) is een object dat alleen wordt gebruikt om gegevens tussen de frontend en backend uit te
wisselen. Het voorkomt dat je meer informatie dan nodig wordt uitgewisseld en maakt de code overzichtelijker.

#### **Implementatie:**

Maak een nieuwe klasse aan genaamd `UserLoginRequestDTO` in de map `src/main/java/nl/novi/cardemo/dtos`.

<details>
<summary>Klik hier om de code te bekijken</summary>

```java
package nl.novi.cardemo.dtos;

public class UserLoginRequestDTO {
    private String userName;
    private String password;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
```

</details>

#### **Uitleg van de velden:**

1. **`userName`:** Dit veld slaat de gebruikersnaam op die door de gebruiker wordt ingevoerd.
2. **`password`:** Dit veld bevat het wachtwoord dat de gebruiker invoert.

Dit resulteert in een json object

````json
{
  "userName": "user",
  "password": "user"
}
````

#### **Waarom een DTO gebruiken?**

- Vermindert het risico op blootstelling van interne modelgegevens.
- Zorgt voor duidelijke en gestructureerde gegevensuitwisseling tussen frontend en backend.
- Maakt toekomstige uitbreidingen eenvoudiger, zoals het toevoegen van validatieregels.

---

### LoginController.java

**Context:**
De `LoginController` biedt een endpoint waarmee gebruikers een JWT-token kunnen verkrijgen door hun gebruikersnaam en
wachtwoord in te voeren.

**Implementatie:**
<details>
<summary>Klik hier om de code te bekijken</summary>

```java
package nl.novi.cardemo.controllers;

import nl.novi.cardemo.security.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import nl.novi.cardemo.dtos.UserLoginRequestDTO;

@RestController
public class LoginController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public LoginController(AuthenticationManager man, JwtService service) {
        this.authManager = man;
        this.jwtService = service;
    }

    @PostMapping("/login")
    public ResponseEntity<String> signIn(@RequestBody UserLoginRequestDTO userLoginRequestDTO
    ) {
        UsernamePasswordAuthenticationToken up =
                new UsernamePasswordAuthenticationToken(userLoginRequestDTO.getUserName(), userLoginRequestDTO.getPassword());

        try {
            Authentication auth = authManager.authenticate(up);

            var ud = (UserDetails) auth.getPrincipal();
            String token = jwtService.generateToken(ud);

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .body("Token generated");
        } catch (AuthenticationException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }
}

```

</details>

---

## Stap 3: Aanpassen van bestaande bestanden

### SecurityConfig.java

**Context:**
De beveiligingsconfiguratie wordt aangepast om JWT te ondersteunen en Basic Auth te verwijderen.

- .httpBasic(Customizer.withDefaults())  -->   .httpBasic(hp -> hp.disable())
- .requestMatchers("/login").permitAll() --> toevoegen om een token te krijgen
- .addFilterBefore(new JwtRequestFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
- voeg de jwtService toe aan de methode voor de dependency injection

**Implementatie:**
<details>
<summary>Klik hier om de code te bekijken</summary>

```java
package nl.novi.cardemo.config;

import nl.novi.cardemo.security.JwtService;
import nl.novi.cardemo.services.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import nl.novi.cardemo.security.JwtRequestFilter;

@Configuration
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {

        http
                .httpBasic(hp -> hp.disable()) //aanpassing
                .authorizeHttpRequests(auth -> auth
                        // Publieke endpoints
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/login").permitAll() //aanpassing

                        .requestMatchers(HttpMethod.GET, "/cars").hasAnyRole("USER")
                        // Beveiligde endpoints: Alleen voor admins
                        .requestMatchers("/cars/**").hasRole("ADMIN")

                        // Andere verzoeken worden geweigerd
                        .anyRequest().denyAll()
                )
                .addFilterBefore(new JwtRequestFilter(jwtService), UsernamePasswordAuthenticationFilter.class) // aanpassing
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

</details>

---

## Stap 4: Testen van de configuratie in Postman

In deze stap leer je hoe je de beveiliging en JWT-authenticatie van de applicatie kunt testen met behulp van Postman.
Dit omvat het verkrijgen van een token en het gebruiken ervan om beveiligde endpoints te benaderen.

### **Context:**

Postman is een handige tool om API’s te testen. We zullen het login-endpoint gebruiken om een JWT-token te verkrijgen,
en vervolgens een beveiligd endpoint benaderen met dat token.

### **Implementatie:**

#### 1. **Inloggen en een token verkrijgen**

1. Open Postman en voeg een nieuwe POST-request toe met de volgende configuratie:
    - **URL:** `{{baseUrl}}/login`
    - **Methode:** POST
    - **Headers:**
        - `Content-Type`: `application/json`
    - **Body (raw):**
      ```json
      {
          "userName": "user",
          "password": "user"
      }
      ```

2. Klik op "Send". Als de inloggegevens correct zijn, ontvang je een JWT-token in de response-header met de
   sleutel `Authorization`. Het ziet er als volgt uit:
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNjg4MjYyMjAwLCJleHAiOjE2ODgyNjU4MDB9.aBcDeFgHijKlMnOpQrStUvWxYz0123456789
   ```

3. Kopieer het token uit de response-header (alles na `Bearer `).

#### 2. **Een beveiligd endpoint benaderen**

1. Voeg een nieuwe GET-request toe in Postman om een beveiligd endpoint te testen. Gebruik bijvoorbeeld:
    - **URL:** `{{baseUrl}}/cars`
    - **Methode:** GET

2. Voeg de volgende headers toe:
    - `Content-Type`: `application/json`
    - `Authorization`: `Bearer <gekopieerd-token>`

3. Klik op "Send". Als het token geldig is en de gebruiker de juiste rechten heeft, ontvang je een response met de
   gegevens van het beveiligde endpoint.

#### 3. **Automatiseren van het tokengebruik**

1. Open de "Tests"-tab in de login-request en voeg de volgende JavaScript-code toe om het token automatisch op te slaan:
   ```javascript
   if (pm.response.headers.has("Authorization")) {
       var header = pm.response.headers.get("Authorization");
       var token = header.split(" ")[1]; // Haalt het token na "Bearer "
       pm.environment.set("token", token);
   }
   ```

2. In beveiligde requests kun je nu verwijzen naar het opgeslagen token:
    - Voeg de header toe:
        - `Authorization`: `Bearer {{token}}`

3. Klik op "Send" en controleer of het token correct wordt gebruikt.

### **Waarom deze aanpak?**

- **Efficiëntie:** Het token wordt automatisch opgeslagen en hergebruikt in Postman.
- **Beveiliging:** Test of de beveiliging correct is ingesteld en alleen geautoriseerde gebruikers toegang hebben.
- **Gebruiksvriendelijkheid:** Simpel en herhaalbaar testproces voor het debuggen van de applicatie.

---

