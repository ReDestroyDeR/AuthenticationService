package ru.red.four.authorizationservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.red.four.authorizationservice.dto.UserDetachedDTO;
import ru.red.four.authorizationservice.service.UserService;

import java.security.Key;

@RestController
@RequestMapping("/")
public class AuthController {

    private final UserService userService;
    private final Key publicKey;

    @Autowired
    public AuthController(UserService userService, @Value("jwt.keys.public") Key publicKey) {
        this.userService = userService;
        this.publicKey = publicKey;
    }

    @PostMapping("login")
    public Mono<String> login(UserDetachedDTO userDetachedDTO) {
        return userService.login(userDetachedDTO);
    }

    @PostMapping("register")
    public Mono<Void> register(UserDetachedDTO userDetachedDTO) {
        return userService.registerUser(userDetachedDTO).then();
    }

    @GetMapping("public-key")
    public Mono<String> publicKey() {
        return Mono.just(publicKey.toString());
    }
}
