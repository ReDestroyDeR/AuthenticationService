package ru.red.four.authorizationservice.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Data Transfer Object used in order to transmit credentials on Network<br>
 * Detached from business logic and thus contains no ID
 */
@Data
@ApiModel(description = "Detached user credentials")
public class UserDetachedDTO {
    @ApiModelProperty(required = true, example = "Username")
    private String username;
    @ApiModelProperty(required = true, example = "Password")
    private String password;
}
