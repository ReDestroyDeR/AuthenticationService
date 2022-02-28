package ru.red.four.authorizationservice.mapper;

import org.springframework.stereotype.Component;
import ru.red.four.authorizationservice.dto.UserDTO;
import ru.red.four.authorizationservice.dto.UserDetachedDTO;
import ru.red.four.authorizationservice.dto.UserIdentityDTO;
import ru.red.four.authorizationservice.jooq.tables.records.UsersRecord;

@Component
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

    /**
     * Maps UsersRecord to UserDetachedDTO
     *
     * @param record input
     * @return {@link UserDetachedDTO}
     */
    @Override
    public UserDetachedDTO usersRecordToUserDetachedDTO(UsersRecord record) {
        UserDetachedDTO userDetachedDTO = new UserDetachedDTO();
        userDetachedDTO.setUsername(record.getUsername());
        userDetachedDTO.setPassword(record.getPassword());
        return userDetachedDTO;
    }

    /**
     * Maps UserDetachedDTO to UsersRecord
     *
     * @param dto input
     * @return {@link UsersRecord}
     */
    @Override
    public UsersRecord userDetachedDtoToUsersRecord(UserDetachedDTO dto) {
        UsersRecord usersRecord = new UsersRecord();
        usersRecord.setUsername(dto.getUsername());
        usersRecord.setPassword(dto.getPassword());
        return usersRecord;
    }

    /**
     * Maps User Detached DTO to User Identity DTO
     *
     * @param dto input
     * @return {@link UserIdentityDTO}
     */
    @Override
    public UserIdentityDTO userDetachedDTOToUserIdentityDTO(UserDetachedDTO dto) {
        UserIdentityDTO identityDTO = new UserIdentityDTO();
        identityDTO.setUsername(dto.getUsername());
        return identityDTO;
    }

    /**
     * Maps User DTO to User Identity DTO
     *
     * @param dto input
     * @return {@link UserIdentityDTO}
     */
    @Override
    public UserIdentityDTO userDTOToUserIdentityDTO(UserDTO dto) {
        UserIdentityDTO identityDTO = new UserIdentityDTO();
        identityDTO.setUsername(dto.getUsername());
        return identityDTO;
    }

    /**
     * Maps Users Record to User Identity DTO
     *
     * @param record input
     * @return {@link UserIdentityDTO}
     */
    @Override
    public UserIdentityDTO usersRecordToUserIdentityDTO(UsersRecord record) {
        UserIdentityDTO identityDTO = new UserIdentityDTO();
        identityDTO.setUsername(record.getUsername());
        return identityDTO;
    }
}
