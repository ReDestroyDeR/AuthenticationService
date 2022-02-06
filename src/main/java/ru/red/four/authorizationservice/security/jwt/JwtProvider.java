package ru.red.four.authorizationservice.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.red.four.authorizationservice.jooq.tables.records.UsersRecord;

import java.security.Key;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Log4j2
@Component
@SuppressWarnings("ClassCanBeRecord")
public class JwtProvider {
    private final Key privateKey;
    private final Long jwtMinutes;

    @Autowired
    public JwtProvider(@Value("jwt.keys.private") Key privateKey,
                       @Value("jwt.claims.exp.minutes") Long jwtMinutes) {
        this.privateKey = privateKey;
        this.jwtMinutes = jwtMinutes;
        log.info("Initialized JWT Provider");
    }

    public String createJwt(UsersRecord record) {
        var id = record.getId();
        var username = record.getUsername();
        log.info("Creating JWT for [{}] {}", id, username);
        LocalDateTime now = LocalDateTime.now();
        return Jwts.builder()
                .setSubject(id.toString())
                .setIssuedAt(Date.from(now.toInstant(ZoneOffset.UTC)))
                .setExpiration(Date.from(
                                now.plusMinutes(jwtMinutes)
                                        .toInstant(ZoneOffset.UTC)
                        )
                )
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
    }
}
