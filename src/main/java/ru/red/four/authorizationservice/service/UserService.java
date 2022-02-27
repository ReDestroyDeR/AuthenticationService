package ru.red.four.authorizationservice.service;

import reactor.core.publisher.Mono;
import ru.red.four.authorizationservice.dto.UserDTO;
import ru.red.four.authorizationservice.dto.UserDetachedDTO;
import ru.red.four.authorizationservice.jooq.tables.records.UsersRecord;

public interface UserService {
    /**
     * Registration function returning created User
     *
     * @param userDetachedDTO credentials
     * @return Persisted {@link UsersRecord}
     */
    Mono<UsersRecord> registerUser(UserDetachedDTO userDetachedDTO);

    /**
     * Login function returning JWT
     * <p>
     * Could be fed with password or refresh token
     *
     * @param userDetachedDTO user data transfer object containing raw credentials
     * @return JWT
     */
    Mono<String> login(UserDetachedDTO userDetachedDTO);

    /**
     * User Update function
     *
     * @param userDTO User credentials with ID
     * @return Persisted {@link UsersRecord}
     */
    Mono<UsersRecord> update(UserDTO userDTO);

    /**
     * Users Delete function
     * Deletes user only if provided DTO has correct password for that user
     *
     * @param userDetachedDTO user instance
     * @return Completion signal, error on failure
     */
    Mono<Void> delete(UserDetachedDTO userDetachedDTO);
}
