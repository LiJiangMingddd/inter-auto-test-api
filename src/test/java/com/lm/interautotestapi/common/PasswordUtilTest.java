package com.lm.interautotestapi.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void encode_shouldReturnBcryptHash() {
        String raw = "test123456";
        String encoded = PasswordUtil.encode(raw);
        assertNotNull(encoded);
        assertTrue(encoded.startsWith("$2a$"));
        assertEquals(60, encoded.length());
    }

    @Test
    void matches_shouldReturnTrueForCorrectPassword() {
        String raw = "myPassword123";
        String encoded = PasswordUtil.encode(raw);
        assertTrue(PasswordUtil.matches(raw, encoded));
    }

    @Test
    void matches_shouldReturnFalseForWrongPassword() {
        String raw = "correctPassword";
        String encoded = PasswordUtil.encode(raw);
        assertFalse(PasswordUtil.matches("wrongPassword", encoded));
    }

    @Test
    void encode_shouldGenerateDifferentHashForSameInput() {
        String raw = "samePassword";
        String encoded1 = PasswordUtil.encode(raw);
        String encoded2 = PasswordUtil.encode(raw);
        assertNotEquals(encoded1, encoded2);
        assertTrue(PasswordUtil.matches(raw, encoded1));
        assertTrue(PasswordUtil.matches(raw, encoded2));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "ab", "abc123", "!@#$%^&*()", "密码123"})
    void encode_shouldHandleVariousInputs(String raw) {
        String encoded = PasswordUtil.encode(raw);
        assertNotNull(encoded);
        assertTrue(encoded.startsWith("$2a$"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void matches_shouldReturnFalseForNullOrEmpty(String raw) {
        String encoded = PasswordUtil.encode("test");
        assertFalse(PasswordUtil.matches(raw, encoded));
    }
}
