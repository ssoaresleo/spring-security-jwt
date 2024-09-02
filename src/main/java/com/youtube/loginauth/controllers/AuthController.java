package com.youtube.loginauth.controllers;

import com.youtube.loginauth.domain.user.User;
import com.youtube.loginauth.dto.LoginRequestDTO;
import com.youtube.loginauth.dto.RegisterRequestDTO;
import com.youtube.loginauth.dto.ResponseLoginDTO;
import com.youtube.loginauth.infra.security.TokenService;
import com.youtube.loginauth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.sql.exec.ExecutionException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginRequestDTO body) {
        User userExists = this.repository.findByEmail(body.email()).orElseThrow(() -> new ExecutionException("User not found"));

        if(passwordEncoder.matches(body.password(), userExists.getPassword())) {
            String token = this.tokenService.generateToken(userExists);
            return ResponseEntity.ok(new ResponseLoginDTO(userExists.getName(), token));
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterRequestDTO body) {

        Optional<User> userExists = this.repository.findByEmail(body.email());

        if(userExists.isEmpty()) {
            User user = new User();
            user.setName(body.name());
            user.setEmail(body.email());
            user.setPassword(passwordEncoder.encode(body.password()));

            this.repository.save(user);

            String token = this.tokenService.generateToken(user);
            return ResponseEntity.ok(new ResponseLoginDTO(user.getName(), token));
        }
        return ResponseEntity.badRequest().build();
    }
}
