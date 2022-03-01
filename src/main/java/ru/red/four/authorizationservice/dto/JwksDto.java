package ru.red.four.authorizationservice.dto;

import lombok.Getter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JwksDto {
    @Getter
    private Set<Map<String, Object>> keys;

    {
        keys = new HashSet<>();
    }

    public void addKey(Map<String, Object> values) {
        keys.add(values);
    }

    public void deleteKey(Map<String, Object> values) {
        keys.remove(values);
    }
}
