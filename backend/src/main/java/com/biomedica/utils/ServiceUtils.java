package com.biomedica.utils;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class ServiceUtils {

    private static final String NUMERIC_CHARACTERS = "0123456789";
    private static final int CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    public String generateRandomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(NUMERIC_CHARACTERS.length());
            code.append(NUMERIC_CHARACTERS.charAt(index));
        }
        return code.toString();
    }

}
