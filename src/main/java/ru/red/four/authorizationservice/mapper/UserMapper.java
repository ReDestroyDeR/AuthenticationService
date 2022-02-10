package ru.red.four.authorizationservice.mapper;

import ru.red.four.authorizationservice.dto.UserDTO;
import ru.red.four.authorizationservice.dto.UserDetachedDTO;
import ru.red.four.authorizationservice.jooq.tables.records.UsersRecord;

public interface UserMapper {
    /**
     * Maps UserDTO to UserDetachedDTO
     *
     * @param dto input
     * @return {@link UserDetachedDTO}
     */
    UserDetachedDTO userDTOToUserDetachedDTO(UserDTO dto);

    /**
     * Maps UserDetachedDTO to UserDTO
     *
     * @param detached input
     * @return {@link UserDTO} with {@link UserDTO#getId()} being null
     */
    UserDTO userDetachedDTOToUserDTO(UserDetachedDTO detached);

    /**
     * Maps UsersRecord to UserDTO
     *
     * @param record input
     * @return {@link UserDTO}
     */
    UserDTO usersRecordToUserDTO(UsersRecord record);

    /**
     * Maps UserDTO to UsersRecord
     *
     * @param dto input
     * @return {@link UsersRecord} with {@link UsersRecord#getSalt()} being null
     */
    UsersRecord userDTOToUsersRecord(UserDTO dto);
}
