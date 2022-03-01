package ru.red.four.authorizationservice.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/.well-known")
public class WellKnownController {
    private final Map<String, Object> publicJwk;

    /**
     * @param publicKey to expose as JWK to outside world
     */
    public WellKnownController(PublicKey publicKey) {
        RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
        this.publicJwk = generateJwk(rsaPublicKey);
    }

    /**
     * JWK exposing endpoint
     *
     * @return <code>Map<String, Object></code> JWK
     */
    @ApiOperation("Fetch JWKs")
    @ApiResponse(
            code = 200,
            message = "JSON of Public JWKs specs",
            response = Set.class
    )
    @GetMapping("jwks.json")
    public Mono<Set<Map<String, Object>>> publicKey() {
        return Mono.just(Set.of(publicJwk));
    }

    private Map<String, Object> generateJwk(RSAPublicKey rsaPublicKey) {
        Map<String, Object> values = new HashMap<>();
        values.put("kty", rsaPublicKey.getAlgorithm());
        values.put("kid", "static"); // TODO : Implement JWK Rotation
        values.put("n", Base64Utils.encodeToUrlSafeString(rsaPublicKey.getModulus().toByteArray()));
        values.put("e", Base64Utils.encodeToUrlSafeString(rsaPublicKey.getPublicExponent().toByteArray()));
        values.put("alg", "RS256");
        values.put("use", "sig");
        return values;
    }
}
