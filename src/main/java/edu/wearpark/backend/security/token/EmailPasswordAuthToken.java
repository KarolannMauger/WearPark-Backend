package edu.wearpark.backend.security.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

/**
 * Authentication token for email and password
 * @version 1.0
 */
public class EmailPasswordAuthToken extends AbstractAuthenticationToken {
    /** The user email*/
    final private String email;
    /** The user password */
    final private String password;

    /**
     * Constructs a new {@code EmailPasswordAuthToken}.
     * @param email the user email
     * @param password the user password
     */
    public EmailPasswordAuthToken(String email, String password) {
        super(Collections.emptyList());
        this.email = email;
        this.password = password;
    }

    /**
     * Returns the credentials of this authentication token.
     * @return the user password
     */
    @Override
    public String getCredentials() {
        return password;
    }

    /**
     * Returns the principal of this authentication token.
     * @return the user email
     */
    @Override
    public String getPrincipal() {
        return email;
    }
}