package ru.red.four.authorizationservice.util;

import ru.red.four.authorizationservice.jooq.tables.records.UsersRecord;

import java.util.Random;

import static ru.red.four.authorizationservice.util.StringUtil.generateRandomString;

public class UserUtils {
    public static UsersRecord createRandomUser() {
        UsersRecord record = new UsersRecord();
        record.setUsername(generateRandomString(10));
        record.setPassword(generateRandomString(10));
        return record;
    }

    public static UsersRecord createRandomUserWithSalt() {
        UsersRecord record = createRandomUser();
        record.setSalt(generateRandomString(10));
        return record;
    }

    public static UsersRecord createRandomUserWithId() {
        UsersRecord record = createRandomUser();
        record.setId(new Random().nextLong(1, 100000));
        return record;
    }

    public static UsersRecord createRandomUserWithSaltAndId() {
        UsersRecord record = createRandomUserWithSalt();
        record.setId(new Random().nextLong(1, 100000));
        return record;
    }
}
