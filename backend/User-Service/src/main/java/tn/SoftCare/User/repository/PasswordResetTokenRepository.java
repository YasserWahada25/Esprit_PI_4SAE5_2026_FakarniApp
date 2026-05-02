package tn.SoftCare.User.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.SoftCare.User.model.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    
    Optional<PasswordResetToken> findByShortCodeAndEmail(String shortCode, String email);

    void deleteByUserId(String userId);

}