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
import nl.novi.cardemo.dtos.users.UserLoginRequestDTO;

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