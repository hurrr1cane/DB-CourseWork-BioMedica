package com.biomedica.repository;

import com.biomedica.entity.user.Token;
import com.biomedica.entity.user.TokenType;
import com.biomedica.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {

    List<Token> findByUser(User user);

    Optional<Token> findByTokenAndTokenType(String token, TokenType tokenType);
}
