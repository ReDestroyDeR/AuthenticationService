package ru.red.four.authorizationservice.service;

import reactor.core.publisher.Mono;
import ru.red.four.authorizationservice.jooq.tables.records.UsersRecord;

public interface UserService {
    Mono<UsersRecord> getUser(Long id);
    Mono<UsersRecord> createUser(UsersRecord record);
    Mono<UsersRecord> updateUser(Long id, UsersRecord record);
    Mono<Integer> deleteUser(Long id);
}
