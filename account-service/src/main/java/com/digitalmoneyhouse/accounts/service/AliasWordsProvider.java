package com.digitalmoneyhouse.accounts.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.List;

@Component
public class AliasWordsProvider {

    private final List<String> words;

    public AliasWordsProvider(
        @Value("classpath:alias-words.txt") Resource resource
    ) throws IOException {
        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream())
            )
        ) {
            words = reader.lines()
                .map(String::trim)
                .filter(word -> !word.isBlank())
                .toList();
        }

        if (words.size() < 3) {
            throw new IllegalStateException(
                "El archivo de alias debe contener al menos tres palabras"
            );
        }
    }

    public String randomWord(SecureRandom secureRandom) {
        return words.get(secureRandom.nextInt(words.size()));
    }
}