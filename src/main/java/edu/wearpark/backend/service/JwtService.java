package edu.wearpark.backend.service;

import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.repository.UserRepository;
import edu.wearpark.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepo;
    public Optional<User> getUserFromAuthToken(String jwt) {
        return jwtUtil.extractClaims(jwt, "auth").flatMap(claims -> {
            return userRepo.findById(new ObjectId(claims.getSubject()));
        });
    }
}
