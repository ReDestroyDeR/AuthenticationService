package ru.red.four.authorizationservice.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.red.four.authorizationservice.dto.UserDTO;
import ru.red.four.authorizationservice.dto.UserDetachedDTO;
import ru.red.four.authorizationservice.exception.BadPasswordException;
import ru.red.four.authorizationservice.jooq.tables.records.UsersRecord;
import ru.red.four.authorizationservice.repository.UserRepository;
import ru.red.four.authorizationservice.security.jwt.JwtProvider;
import ru.red.four.authorizationservice.security.jwt.JwtValidator;
import ru.red.four.authorizationservice.util.RandomStringUtil;

@Log4j2
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final Integer saltLength;

    @Autowired
    public UserServiceImpl(UserRepository repository,
                           JwtProvider jwtProvider,
                           PasswordEncoder passwordEncoder,
                           @Value("${auth.security.salt.length}") Integer saltLength) {
        this.repository = repository;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
        this.saltLength = saltLength;
    }

    @Override
    public Mono<UsersRecord> registerUser(UserDetachedDTO userDetachedDTO) {
        UsersRecord record = new UsersRecord();
        String username = userDetachedDTO.getUsername();
        record.setUsername(username);
        String salt = RandomStringUtil.generateRandomString(this.saltLength);
        String password = passwordEncoder.encode(record.getPassword().concat(salt));
        record.setSalt(salt);
        record.setPassword(password);
        return repository.createUser(record)
                .doOnSuccess(s -> log.info("Account created [{}] {}", s.getId(), username))
                .doOnError(e -> log.info("Account creation failed for {} {}", username, e.getMessage()));
    }

    @Override
    public Mono<String> login(UserDetachedDTO userDetachedDTO) {
        // TODO: Add refresh token functionality
        return repository.getUser(userDetachedDTO.getUsername())
                .flatMap(user -> passwordEncoder.matches(
                        userDetachedDTO.getPassword().concat(user.getSalt()),
                        user.getPassword())
                        ? Mono.just(jwtProvider.createJwt(user))
                        : Mono.error(new BadPasswordException()));
    }

    // TODO: Data manipulating functionality

    @Override
    public Mono<UsersRecord> update(UserDTO userDTO) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Boolean> delete(Long id) {
        throw new UnsupportedOperationException();
    }
}
