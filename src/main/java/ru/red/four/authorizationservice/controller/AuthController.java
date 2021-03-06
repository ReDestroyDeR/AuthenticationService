package ru.red.four.authorizationservice.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.red.four.authorizationservice.dto.UserDetachedDTO;
import ru.red.four.authorizationservice.exception.BadPasswordException;
import ru.red.four.authorizationservice.exception.BadRequestException;
import ru.red.four.authorizationservice.service.UserService;

/**
 * Controller responsible for handling user registration, patching, signing-in <i>Handing out JWTs</i> and removal.
 * All endpoints have no authority checks so 403 must be impossible
 */
@RestController
@RequestMapping("/")
public class AuthController {

    private final UserService userService;

    /**
     * @param userService service implementation used as backend for all operations
     */
    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint serving JWTs
     *
     * @param userDetachedDTO user credentials containing user and password pair
     * @return 200 {@link String} JSON Web Token that will be used in higher-level authentication<br>
     * 403 {@link BadPasswordException} If credentials are invalid
     */
    @ApiOperation("Receive JWT Token")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "OK",
                    response = String.class,
                    reference = "JWT"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Bad Credentials",
                    response = BadPasswordException.class
            )
    })
    @PostMapping("login")
    public Mono<String> login(@RequestBody UserDetachedDTO userDetachedDTO) {
        return userService.login(userDetachedDTO);
    }

    /**
     * Registration endpoint
     *
     * @param userDetachedDTO user credentials
     * @return 200 {@link Void} User has been created<br>
     * 400 {@link BadRequestException} Username is already occupied
     */
    @ApiOperation("Register new user")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Created new user"
            ),
            @ApiResponse(
                    code = 400,
                    message = "User with this username already exists",
                    response = BadRequestException.class
            )
    })
    @PostMapping("register")
    public Mono<Void> register(@RequestBody UserDetachedDTO userDetachedDTO) {
        return userService.registerUser(userDetachedDTO).then();
    }

    @PostMapping("change-username")
    public Mono<Void> changeUsername(@RequestParam("username") String username,
                                     @RequestBody UserDetachedDTO userDetachedDTO) {
        return userService.updateUsername(username, userDetachedDTO).then();
    }

    @PostMapping("change-password")
    public Mono<Void> changePassword(@RequestParam("password") String password,
                                     @RequestBody UserDetachedDTO userDetachedDTO) {
        return userService.updatePassword(password, userDetachedDTO).then();
    }

    @DeleteMapping("delete")
    public Mono<Void> delete(@RequestBody UserDetachedDTO userDetachedDTO) {
        return userService.delete(userDetachedDTO);
    }
}
