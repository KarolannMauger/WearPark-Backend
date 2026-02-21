package edu.wearpark.backend.service;

import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.repository.UserRepository;
import edu.wearpark.backend.util.JwtUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class JwtService {
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    UserRepository userRepo;
    public Optional<User> getUserFromAuthToken(String jwt) {
        return jwtUtil.extractClaims(jwt, "auth").flatMap(claims -> userRepo.findById(new ObjectId(claims.getSubject())));
    }
}
