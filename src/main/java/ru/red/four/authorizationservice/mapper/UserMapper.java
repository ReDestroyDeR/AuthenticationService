package ru.red.four.authorizationservice.mapper;

import ru.red.four.authorizationservice.dto.UserDTO;
import ru.red.four.authorizationservice.dto.UserDetachedDTO;
import ru.red.four.authorizationservice.jooq.tables.records.UsersRecord;

public interface UserMapper {
    UserDetachedDTO userDTOToUserDetachedDTO(UserDTO dto);
    UserDTO userDetachedDTOToUserDTO(UserDetachedDTO detached);

    UserDTO usersRecordToUserDTO(UsersRecord record);
    UsersRecord userDTOToUsersRecord(UserDTO dto);
}
