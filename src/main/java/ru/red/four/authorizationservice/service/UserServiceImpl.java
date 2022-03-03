package ru.red.four.authorizationservice.service;

import lombok.extern.log4j.Log4j2;
import org.jooq.exception.DataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
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
        if (userDetachedDTO.getUsername() == null || userDetachedDTO.getUsername().length() < 3) {
            return Mono.error(new BadRequestException("Username length must be >= 3 characters"));
        }

        UsersRecord record = userMapper.userDetachedDtoToUsersRecord(userDetachedDTO);
        updateSaltPasswordPair(userDetachedDTO.getPassword(), record);
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
                .switchIfEmpty(Mono.error(NotFoundException::new))
                .flatMap(user -> passwordEncoder.matches(
                        userDetachedDTO.getPassword().concat(user.getSalt()),
                        user.getPassword())
                        ? Mono.just(jwtProvider.createJwt(user))
                        : Mono.error(new BadPasswordException()));
    }

    @Override
    public Mono<UsersRecord> updateUsername(String username, UserDetachedDTO dto) {
        return validatePassword(dto)
                .flatMap(user -> {
                    user.setUsername(username);
                    return repository.updateUser(user.getId(), user);
                })
                .onErrorMap(e -> e.getClass().equals(DataAccessException.class) ? new BadRequestException(e) : e)
                .doOnSuccess(s -> log.info(
                        "Successfully updated username for user [{}] {} -> {}",
                        s.getId(),
                        dto.getUsername(),
                        s.getUsername()))
                .doOnError(e -> log.info(
                        "Failed updating username for user {} -> {} {}",
                        dto.getUsername(),
                        username,
                        e.getMessage()
                ))
                .flatMap(user -> businessServiceWebClient.patch()
                        .uri(uriBuilder -> uriBuilder
                                .path(businessCreateEndpoint + "/change-username")
                                .queryParam("previous_username", dto.getUsername())
                                .queryParam("new_username", username)
                                .build())
                        .exchangeToMono(ClientResponse::releaseBody)
                        .onErrorMap(ExternalServiceException::new)
                        .retry(3)
                        .timeout(Duration.ofSeconds(30))
                        .doOnSuccess(s -> log.info(
                                "Successfully updated username for user {} -> {} on external service",
                                dto.getUsername(),
                                username
                        ))
                        .doOnError(e -> log.info(
                                "Failed updating username for user {} -> {} on external service {}",
                                dto.getUsername(),
                                username,
                                e.getMessage()
                        ))
                        .thenReturn(user));
    }

    @Override
    public Mono<UsersRecord> updatePassword(String password, UserDetachedDTO dto) {
        return validatePassword(dto)
                .flatMap(user -> {
                    updateSaltPasswordPair(password, user);
                    return repository.updateUser(user.getId(), user);
                })
                .doOnSuccess(s -> log.info(
                        "Successfully updated password for user [{}] {}",
                        s.getId(),
                        s.getUsername()
                ))
                .doOnError(e -> log.info(
                        "Failed updating password for user {} {}",
                        dto.getUsername(),
                        e.getMessage()
                ));
    }

    @Override
    public Mono<Void> delete(UserDetachedDTO userDetachedDTO) {
        return validatePassword(userDetachedDTO)
                .flatMap(user -> repository.deleteUser(user.getId()))
                .flatMap(i -> i == 1
                        ? Mono.just(i)
                        : Mono.error(new NotFoundException("Can't delete user " + userDetachedDTO.getUsername())))
                .doOnSuccess(s -> log.info("Successfully deleted user {}", userDetachedDTO.getUsername()))
                .doOnError(e -> log.info("Failed deleting user {} {}", userDetachedDTO.getUsername(), e.getMessage()))
                .then(businessServiceWebClient.delete()
                        .uri(uriBuilder -> uriBuilder // We expect Business Service to follow API Contract
                                .path(businessCreateEndpoint)
                                .queryParam("username", userDetachedDTO.getUsername())
                                .build())
                        .exchangeToMono(ClientResponse::releaseBody)
                        .onErrorMap(ExternalServiceException::new))
                .retry(3)
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(empty -> log.info("Deleted user {} on business service",
                        userDetachedDTO.getUsername()))
                .doOnError(e -> log.info("Failed deleting user on external business service {}", e.getMessage()))
                .then();
    }

    private void updateSaltPasswordPair(String password, UsersRecord record) {
        record.setSalt(StringUtil.generateRandomString(this.saltLength));
        record.setPassword(passwordEncoder.encode(password.concat(record.getSalt())));
    }

    private Mono<UsersRecord> validatePassword(UserDetachedDTO userDetachedDTO) {
        return repository.getUser(userDetachedDTO.getUsername())
                .switchIfEmpty(Mono.error(NotFoundException::new))
                .flatMap(user -> passwordEncoder.matches(
                                userDetachedDTO.getPassword() + user.getSalt(),
                                user.getPassword()
                        ) ? Mono.just(user) : Mono.error(new BadPasswordException("Bad password"))
                );
    }
}
