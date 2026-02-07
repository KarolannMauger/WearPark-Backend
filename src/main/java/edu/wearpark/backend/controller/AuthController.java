package edu.wearpark.backend.controller;

import edu.wearpark.backend.ErrorCode;
import edu.wearpark.backend.dto.JwtResponse;
import edu.wearpark.backend.dto.LoginRequest;
import edu.wearpark.backend.dto.RegisterRequest;
import edu.wearpark.backend.exception.AppException;
import edu.wearpark.backend.security.token.EmailPasswordAuthToken;
import edu.wearpark.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    AuthService authService;
    @PostMapping("/login")
    ResponseEntity<JwtResponse> login(@RequestBody LoginRequest loginRequest) {
        var jwt = authService.login(new EmailPasswordAuthToken(
                loginRequest.email(),
                loginRequest.password()
        ));
        return ResponseEntity.ok(new JwtResponse(jwt));
    }
    @PostMapping("/register")
    ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        var user = authService.register(registerRequest.email(), registerRequest.password());
        String emailValidation = authService.generateEmailValidation(user.getId());
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/validate-email/{jwt}")
    ResponseEntity<?> verifyEmail(
            @PathVariable("jwt") String jwt
    ) {
        if(authService.validateEmail(jwt)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return ResponseEntity.noContent().build();
    }
}
