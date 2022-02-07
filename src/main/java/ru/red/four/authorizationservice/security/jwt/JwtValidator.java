package ru.red.four.authorizationservice.security.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.security.PublicKey;

@Log4j2
@Component
public class JwtValidator {
    private final JwtParser parser;

    public JwtValidator(PublicKey publicKey) {
        this.parser = Jwts.parser().setSigningKey(publicKey);
        log.info("Initialized JWT Validator");
    }

    public boolean validateJwt(String jws) {
        try {
            parser.parseClaimsJws(jws);
        } catch (JwtException e) {
            log.info("Failed validating JWT {}", e.getMessage());
            return false;
        }
        return true;
    }
}
