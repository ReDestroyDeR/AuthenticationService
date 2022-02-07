package ru.red.four.authorizationservice.mapper;

import ru.red.four.authorizationservice.dto.UserDTO;
import ru.red.four.authorizationservice.dto.UserDetachedDTO;
import ru.red.four.authorizationservice.jooq.tables.records.UsersRecord;

public class UserMapperImpl implements UserMapper {
    /**
     * Maps UserDTO to UserDetachedDTO
     *
     * @param dto input
     * @return {@link UserDetachedDTO}
     */
    @Override
    public UserDetachedDTO userDTOToUserDetachedDTO(UserDTO dto) {
        UserDetachedDTO detached = new UserDetachedDTO();
        detached.setUsername(dto.getUsername());
        detached.setPassword(dto.getPassword());
        return detached;
    }

    /**
     * Maps UserDetachedDTO to UserDTO
     *
     * @param detached input
     * @return {@link UserDTO} with {@link UserDTO#getId()} being null
     */
    @Override
    public UserDTO userDetachedDTOToUserDTO(UserDetachedDTO detached) {
        UserDTO dto = new UserDTO();
        dto.setUsername(detached.getUsername());
        dto.setPassword(detached.getPassword());
        return dto;
    }

    /**
     * Maps UsersRecord to UserDTO
     *
     * @param record input
     * @return {@link UserDTO}
     */
    @Override
    public UserDTO usersRecordToUserDTO(UsersRecord record) {
        UserDTO dto = new UserDTO();
        dto.setId(record.getId());
        dto.setUsername(record.getUsername());
        dto.setPassword(record.getPassword());
        return dto;
    }

    /**
     * Maps UserDTO to UsersRecord
     *
     * @param dto input
     * @return {@link UsersRecord} with {@link UsersRecord#getSalt()} being null
     */
    @Override
    public UsersRecord userDTOToUsersRecord(UserDTO dto) {
        UsersRecord record = new UsersRecord();
        record.setId(dto.getId());
        record.setUsername(dto.getUsername());
        record.setPassword(dto.getPassword());
        return record;
    }
}
