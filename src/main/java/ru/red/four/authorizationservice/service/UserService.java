package ru.red.four.authorizationservice.service;

import reactor.core.publisher.Mono;
import ru.red.four.authorizationservice.dto.UserDTO;
import ru.red.four.authorizationservice.dto.UserDetachedDTO;
import ru.red.four.authorizationservice.jooq.tables.records.UsersRecord;

public interface UserService {
    Mono<UsersRecord> registerUser(UserDetachedDTO userDetachedDTO);

    /**
     * Login function returning JWT
     *
     * Could be fed with password or refresh token
     * @param userDetachedDTO user data transfer object containing raw credentials
     * @return JWT
     */
    Mono<String> login(UserDetachedDTO userDetachedDTO);

    Mono<UsersRecord> update(UserDTO userDTO);

    Mono<Boolean> delete(Long id);
}
