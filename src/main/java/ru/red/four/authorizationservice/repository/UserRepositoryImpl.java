package ru.red.four.authorizationservice.repository;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.red.four.authorizationservice.jooq.tables.records.UsersRecord;

import static ru.red.four.authorizationservice.jooq.tables.Users.USERS;


@Repository
public class UserRepositoryImpl implements UserRepository {
    private final DSLContext jooq;

    @Autowired
    public UserRepositoryImpl(DSLContext jooq) {
        this.jooq = jooq;
    }

    @Override
    public Mono<UsersRecord> getUser(Long id) {
        return Mono.create(sink -> {
            try {
                sink.success(jooq.selectFrom(USERS)
                        .where(USERS.ID.eq(id))
                        .fetchOne());
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    @Override
    public Mono<UsersRecord> getUser(String username) {
        return Mono.create(sink -> {
            try {
                sink.success(jooq.selectFrom(USERS)
                        .where(USERS.USERNAME.eq(username))
                        .fetchOne());
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    @Override
    public Mono<UsersRecord> createUser(UsersRecord record) {
        return Mono.create(sink -> {
            try {
                record.changed(USERS.ID, false);
                sink.success(jooq.insertInto(USERS)
                        .set(record)
                        .returning()
                        .fetchOne());
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    @Override
    public Mono<UsersRecord> updateUser(Long id, UsersRecord record) {
        return Mono.create(sink -> {
            try {
                sink.success(jooq.update(USERS)
                        .set(record)
                        .where(USERS.ID.eq(id))
                        .returning()
                        .fetchOne());
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    @Override
    public Mono<Integer> deleteUser(Long id) {
        return Mono.create(sink -> {
            try {
                sink.success(jooq.delete(USERS)
                        .where(USERS.ID.eq(id))
                        .execute());
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
}
