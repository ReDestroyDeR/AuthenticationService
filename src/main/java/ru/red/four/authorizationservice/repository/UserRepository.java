package ru.red.four.authorizationservice.repository;

import reactor.core.publisher.Mono;
import ru.red.four.authorizationservice.jooq.tables.records.UsersRecord;

public interface UserRepository {
    Mono<UsersRecord> getUser(Long id);

    Mono<UsersRecord> getUser(String username);

    Mono<UsersRecord> createUser(UsersRecord record);

    Mono<UsersRecord> updateUser(Long id, UsersRecord record);

    Mono<Integer> deleteUser(Long id);
}
