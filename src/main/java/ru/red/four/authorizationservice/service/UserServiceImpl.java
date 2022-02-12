package ru.red.four.authorizationservice.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.red.four.authorizationservice.dto.UserDTO;
import ru.red.four.authorizationservice.dto.UserDetachedDTO;
import ru.red.four.authorizationservice.exception.BadPasswordException;
import ru.red.four.authorizationservice.exception.BadRequestException;
import ru.red.four.authorizationservice.exception.ExternalServiceException;
import ru.red.four.authorizationservice.jooq.tables.records.UsersRecord;
import ru.red.four.authorizationservice.mapper.UserMapper;
import ru.red.four.authorizationservice.repository.UserRepository;
import ru.red.four.authorizationservice.security.jwt.JwtProvider;
import ru.red.four.authorizationservice.util.StringUtil;

import java.time.Duration;

@Log4j2
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final WebClient businessServiceWebClient;
    private final UserMapper userMapper;
    private final JwtProvider jwtProvider;
    private final Integer saltLength;
    private final String businessCreateEndpoint;

    @Autowired
    public UserServiceImpl(UserRepository repository,
                           JwtProvider jwtProvider,
                           PasswordEncoder passwordEncoder,
                           WebClient businessServiceWebClient,
                           UserMapper userMapper,
                           @Value("${auth.security.salt.length}") Integer saltLength,
                           @Value("${external.business.endpoints.create}") String createEndpoint) {
        this.repository = repository;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.saltLength = saltLength;
        this.businessServiceWebClient = businessServiceWebClient;
        this.businessCreateEndpoint = createEndpoint;
    }

    @Override
    public Mono<UsersRecord> registerUser(UserDetachedDTO userDetachedDTO) {
        UsersRecord record = new UsersRecord();
        String username = userDetachedDTO.getUsername();
        record.setUsername(username);
        String salt = StringUtil.generateRandomString(this.saltLength);
        String password = passwordEncoder.encode(userDetachedDTO.getPassword().concat(salt));
        record.setSalt(salt);
        record.setPassword(password);
        return repository.createUser(record)
                .onErrorMap(BadRequestException::new)
                .doOnSuccess(s -> log.info("Account created [{}] {}", s.getId(), username))
                .doOnError(e -> log.info("Account creation failed for {} {}", username, e.getMessage()))
                .flatMap(usersRecord -> businessServiceWebClient.post()
                                .uri(businessCreateEndpoint)
                                .bodyValue(userMapper.usersRecordToUserIdentityDTO(usersRecord))
                                .exchangeToMono(response -> response.statusCode().isError()
                                        ? Mono.error(new ExternalServiceException(response.statusCode().getReasonPhrase()))
                                        : Mono.empty())
                                .doOnSuccess(s ->
                                        log.info("Successfully created account [{}] {} in Business service",
                                                usersRecord.getId(),
                                                username))
                                .doOnError(e ->
                                        log.info("Failed creating account [{}] {} in Business service {}",
                                                usersRecord.getId(),
                                                username,
                                                e.getMessage())
                                )
                                .retry(3)
                                .timeout(Duration.ofSeconds(30))
                                .thenReturn(usersRecord)
                        // .doOnError() Delete user
                );
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
