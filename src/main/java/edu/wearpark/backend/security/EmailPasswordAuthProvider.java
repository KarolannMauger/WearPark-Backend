package edu.wearpark.backend.security;

import edu.wearpark.backend.ErrorCode;
import edu.wearpark.backend.domain.PasswordAuth;
import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.exception.NotFoundException;
import edu.wearpark.backend.repository.UserRepository;
import edu.wearpark.backend.security.token.DetailedAuthToken;
import edu.wearpark.backend.security.token.EmailPasswordAuthToken;
import edu.wearpark.backend.util.DurationCodec;
import edu.wearpark.backend.exception.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;

/**
 * Authentication provider that handles user authentication form email and password.
 * @version 1.0
 */
@Component
public class EmailPasswordAuthProvider implements AuthenticationProvider {
    final private Integer PWD_ATTEMPTS_MAX;
    final private Duration PWD_ATTEMPTS_LOCKOUT;
    final private Duration VERIFIED_EXPIRATION = DurationCodec.decode("t1h");
    @Autowired
    PasswordEncoder pwdEncoder;
    @Autowired
    UserRepository userRepo;

    /**
     * Constructs the authentication provider with configuration values.
     *
     * @param pwdAttemptsMax maximum password attempts before lockout from app properties
     * @param pwdAttemptsLockout duration of lockout period from app properties
     */
    public EmailPasswordAuthProvider(
            @Value("${auth.providers.email-password.max-attempts}") String pwdAttemptsMax,
            @Value("${auth.providers.email-password.lockout-duration}") String pwdAttemptsLockout
    ) {
        PWD_ATTEMPTS_MAX = Integer.parseInt(pwdAttemptsMax);
        PWD_ATTEMPTS_LOCKOUT = DurationCodec.decode(pwdAttemptsLockout);
    }
    private void tryVerifiedUser(User user) {
        // TODO: try to verify user
    }

    /**
     * Checks the user is currently locked out due to many failed login attempts
     * @param user the user to check
     * @throws LockedException if the user is locked out
     */
    private void tryUserLockout(User user) {
        PasswordAuth pwd = user.getAuth();
        if(PWD_ATTEMPTS_MAX > 0 &&
                pwd.getAttempts() >= PWD_ATTEMPTS_MAX &&
                pwd.getLastAttempt().isAfter(Instant.now().minus(PWD_ATTEMPTS_LOCKOUT))
        ) {
            throw new AppException(ErrorCode.LOCKED_OUT);
        }
    }

    /**
     * Verifies that the provider password matches the stored passW digest.
     * @param user the user to authenticate
     * @param plainPassword the plain-text password does not match
     * @throws BadCredentialsException if the passW does not match
     */
    private void tryMatchingPassword(User user, String plainPassword) {
        PasswordAuth pwd = user.getAuth();
        if(pwdEncoder.matches(plainPassword, pwd.getHash())) {
            pwd.setAttempts(0);
            pwd.setLastAttempt(Instant.now());
            userRepo.save(user);
        } else {
            pwd.setAttempts(pwd.getAttempts()+1);
            pwd.setLastAttempt(Instant.now());
            userRepo.save(user);
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }
    }

    /**
     * Performs email-password authenticate for given token
     * @param token the {@link EmailPasswordAuthToken} containing user credentials
     * @return a {@link  DetailedAuthToken} if authentication succeeds
     */
    private DetailedAuthToken authEmailPassword(final EmailPasswordAuthToken token) {
        var result = userRepo.findByEmail(token.getPrincipal());
        if(result.isEmpty())
            throw new NotFoundException("user", token.getPrincipal());
        User user = result.get();
        tryUserLockout(user);
        tryMatchingPassword(user, token.getCredentials());
        return new DetailedAuthToken(user, Collections.emptyList());
    }

    /**
     * Authentication a given {@link Authentication} token
     * @param authToken the authentication token
     * @return the authenticated token if successful
     * @throws AppException if the token type is unsupported or authentication fails
     */
    @Override
    public DetailedAuthToken authenticate(final Authentication authToken) throws AppException {
        if(authToken instanceof EmailPasswordAuthToken)
            return authEmailPassword((EmailPasswordAuthToken) authToken);
        throw new AuthenticationCredentialsNotFoundException("Authentication method not found");
    }

    /**
     * Indicates the provider supports the given authentication class.
     * @param authClass the class of the authentication token
     * @return {@code  true} if the provider supports {@link EmailPasswordAuthToken} {@code  false} otherwise
     */
    @Override
    public boolean supports(Class<?> authClass) {
        return authClass.equals(EmailPasswordAuthToken.class);
    }
}
