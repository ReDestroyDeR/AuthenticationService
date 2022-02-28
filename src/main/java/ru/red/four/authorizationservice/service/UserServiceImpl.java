package ru.red.four.authorizationservice.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.red.four.authorizationservice.dto.UserDetachedDTO;
import ru.red.four.authorizationservice.exception.BadPasswordException;
import ru.red.four.authorizationservice.exception.BadRequestException;
import ru.red.four.authorizationservice.exception.ExternalServiceException;
import ru.red.four.authorizationservice.exception.NotFoundException;
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
        UsersRecord record = userMapper.userDTOToUsersRecord(userMapper.userDetachedDTOToUserDTO(userDetachedDTO));
        record.setSalt(StringUtil.generateRandomString(this.saltLength));
        record.setPassword(passwordEncoder.encode(userDetachedDTO.getPassword().concat(record.getSalt())));
        return repository.createUser(record)
                .onErrorMap(BadRequestException::new)
                .doOnSuccess(s -> log.info("Account created [{}] {}", s.getId(), record.getUsername()))
                .doOnError(e -> log.info("Account creation failed for {} {}", record.getUsername(), e.getMessage()))
                .flatMap(usersRecord -> businessServiceWebClient.post()
                                .uri(businessCreateEndpoint)
                                .bodyValue(userMapper.usersRecordToUserIdentityDTO(usersRecord))
                                .exchangeToMono(response -> response.statusCode().isError()
                                        ? Mono.error(new ExternalServiceException(response.statusCode().getReasonPhrase()))
                                        : Mono.empty())
                                .doOnSuccess(s ->
                                        log.info("Successfully created account [{}] {} in Business service",
                                                usersRecord.getId(),
                                                record.getUsername()))
                                .retry(3)
                                .timeout(Duration.ofSeconds(30))
                                .publishOn(Schedulers.boundedElastic())
                                .onErrorMap(e -> {
                                    log.info("Failed creating account [{}] {} in Business service {}",
                                            usersRecord.getId(),
                                            record.getUsername(),
                                            e.getMessage());
                                    delete(userDetachedDTO).subscribe();
                                    return new ExternalServiceException(
                                            "Can't reach external service did rollback for %s".formatted(record.getUsername()),
                                            e);
                                })
                                .thenReturn(usersRecord)
                        // Process failed creation on external service
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

    @Override
    public Mono<UsersRecord> updateUsername(String username, UserDetachedDTO dto) {
        return null;
    }

    @Override
    public Mono<UsersRecord> updatePassword(String password, UserDetachedDTO dto) {
        return null;
    }

    @Override
    public Mono<Void> delete(UserDetachedDTO userDetachedDTO) {
        return repository.getUser(userDetachedDTO.getUsername())
                .switchIfEmpty(Mono.error(NotFoundException::new))
                .flatMap(user -> {
                    boolean matches = passwordEncoder.matches(
                            userDetachedDTO.getPassword() + user.getSalt(),
                            user.getPassword());
                    if (!matches) {
                        return Mono.error(new BadRequestException("Bad password"));
                    }
                    return repository.deleteUser(user.getId());
                })
                .flatMap(i -> i == 1
                        ? Mono.just(i)
                        : Mono.error(new NotFoundException("Can't delete user " + userDetachedDTO.getUsername())))
                .doOnSuccess(s -> log.info("Successfully deleted user {}", userDetachedDTO.getUsername()))
                .doOnError(e -> log.info("Failed deleting user {} {}", userDetachedDTO.getUsername(), e))
                .then(businessServiceWebClient.delete()
                        .uri(uriBuilder -> uriBuilder // We expect Business Service to follow API Contract
                                .path(businessCreateEndpoint)
                                .queryParam("username", userDetachedDTO.getUsername())
                                .build())
                        .exchangeToMono(clientResponse -> Mono.just(clientResponse.statusCode().isError())))
                .doOnSuccess(isError -> log.info("Deleted ({}) user {} on business service",
                        isError,
                        userDetachedDTO.getUsername()))
                .doOnError(e -> log.info("Failed deleting user on external business service {}", e.getMessage()))
                .then();
    }
}
