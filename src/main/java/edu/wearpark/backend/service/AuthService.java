package edu.wearpark.backend.service;

import edu.wearpark.backend.ErrorCode;
import edu.wearpark.backend.domain.PasswordAuth;
import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.exception.AppException;
import edu.wearpark.backend.exception.BadRequestException;
import edu.wearpark.backend.repository.UserRepository;
import edu.wearpark.backend.security.token.DetailedAuthToken;
import edu.wearpark.backend.util.CommonValidationUtil;
import edu.wearpark.backend.util.JwtUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class AuthService {
    @Autowired
    AuthenticationManager authManager;
    @Autowired
    UserRepository userRepo;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    PasswordEncoder pwdEncoder;
    public String login(AbstractAuthenticationToken authToken) throws AppException {
        DetailedAuthToken detailedToken =  (DetailedAuthToken) authManager.authenticate(authToken);
        return jwtUtil.generateToken(
                detailedToken.getPrincipal().getId().toHexString(),
                "auth"
        );
    }
    public User register(String email, String plain_password) {
        if(!CommonValidationUtil.validateEmail(email))
            throw new BadRequestException("Email is not valid");
        if(!CommonValidationUtil.validatePassword(plain_password))
            throw new BadRequestException("Password is not valid");
        if(userRepo.findByEmail(email).isPresent())
            throw new AppException(ErrorCode.EMAIL_CONFLICT);

        var auth = PasswordAuth.builder()
                .attempts(0)
                .lastAttempt(Instant.now())
                .hash(pwdEncoder.encode(plain_password))
                .build();
        var user = User.builder()
                .email(email)
                .role("USER")
                .isEmailValidated(false)
                .auth(auth)
                .build();
        return userRepo.save(user);
    }
    public boolean validateEmail(String jwt) {
        var claims = jwtUtil.extractClaims(jwt, "validate_email");
        if(claims.isEmpty())
            return false;
        var userOptional = userRepo.findById(new ObjectId(claims.get().getSubject()));
        if(userOptional.isEmpty())
            return false;
        var user = userOptional.get();
        user.setIsEmailValidated(true);
        userRepo.save(user);
        return true;
    }
    public String generateEmailValidation(ObjectId userId) {
        return jwtUtil.generateToken(
                userId.toHexString(),
                Instant.now().plus(Duration.ofDays(1)),
                "validate_email"
        );
    }
}
