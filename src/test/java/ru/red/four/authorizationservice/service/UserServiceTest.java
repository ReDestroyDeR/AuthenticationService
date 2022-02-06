package ru.red.four.authorizationservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.AutoConfigureDataJdbc;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.red.four.authorizationservice.jooq.tables.records.UsersRecord;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
@AutoConfigureDataJdbc
class UserServiceTest {
    @Autowired
    UserService service;

    private UsersRecord createTestUsersRecord() {
        UsersRecord record = new UsersRecord();
        record.setUsername("Test username");
        record.setSalt("Test salt");
        record.setPassword("Test password");
        return record;
    }

    private Long createTestUserGetId(UsersRecord record) {
        Long id = service.createUser(record).map(UsersRecord::getId).block();
        return id;
    }

    @Test
    void createUser() {
        UsersRecord record = createTestUsersRecord();
        Long id = createTestUserGetId(record);
        record.setId(id);
        assertEquals(record, service.getUser(id).block());
    }

    @Test
    void deleteUser() {
        UsersRecord record = createTestUsersRecord();
        Long id = createTestUserGetId(record);
        assertEquals(1, service.deleteUser(id).block());
    }

    @Test
    void updateUser() {
        UsersRecord record = createTestUsersRecord();
        Long id = createTestUserGetId(record);
        String text = "New salt";
        record.setSalt(text);
        service.updateUser(id, record).block();
        assertEquals(text, service.getUser(id).block().getSalt());
    }
}