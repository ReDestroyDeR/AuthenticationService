package ru.red.four.authorizationservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.red.four.authorizationservice.dto.UserDetachedDTO;
import ru.red.four.authorizationservice.service.UserService;

import java.security.PublicKey;
import java.util.Base64;

import static ru.red.four.authorizationservice.util.StringUtil.wrapString;

@RestController
@RequestMapping("/")
public class AuthController {

    private final UserService userService;
    private final String publicKey;

    @Autowired
    public AuthController(UserService userService, PublicKey publicKey) {
        this.userService = userService;
        this.publicKey = "-----BEGIN PUBLIC KEY-----" +
                wrapString(new String(Base64.getEncoder().encode(publicKey.getEncoded())), 64) +
                "\n-----END PUBLIC KEY-----\n";
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
        return Mono.just(publicKey);
    }
}
