package ru.red.four.authorizationservice.security.jwt;

import io.jsonwebtoken.Jwts;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.red.four.authorizationservice.configuration.PostgresContainer;
import ru.red.four.authorizationservice.jooq.tables.records.UsersRecord;

import java.security.PublicKey;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static ru.red.four.authorizationservice.util.UserUtils.createRandomUserWithSaltAndId;

@SpringBootTest(webEnvironment = NONE)
@Testcontainers
@ActiveProfiles("test")
class JwtProviderTest {
    @ClassRule
    public final static PostgreSQLContainer<?> postgresSQLContainer = PostgresContainer.getInstance();

    @BeforeAll
    public static void startContainers() {
        postgresSQLContainer.start();
    }

    @Autowired
    JwtProvider provider;

    @Autowired
    JwtValidator validator;

    @Autowired
    PublicKey publicKey;

    @Test
    void doesJwtClaimsMatchOnesOnRecord() {
        UsersRecord record = createRandomUserWithSaltAndId();
        String jwt = provider.createJwt(record);
        assertEquals(record.getUsername(),
                Jwts.parser().setSigningKey(publicKey)
                        .parseClaimsJws(jwt)
                        .getBody()
                        .getSubject()
        );
    }

    @Test
    void willExpiredJwtWork() {
        UsersRecord record = createRandomUserWithSaltAndId();
        var stub = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        MockedStatic<LocalDateTime> localDateTimeMockedStatic = Mockito.mockStatic(LocalDateTime.class);
        localDateTimeMockedStatic.when(LocalDateTime::now)
                .thenReturn(stub)
                .thenCallRealMethod();
        String jwt = provider.createJwt(record);
        localDateTimeMockedStatic.close();
        assertFalse(validator.validateJwt(jwt));
    }
}