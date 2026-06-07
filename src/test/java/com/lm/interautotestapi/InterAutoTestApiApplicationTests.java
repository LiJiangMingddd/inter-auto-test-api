package com.lm.interautotestapi;

import com.lm.interautotestapi.common.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class InterAutoTestApiApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testPasswordUtil() {
        String raw = "test123456";
        String encoded = PasswordUtil.encode(raw);
        assertNotEquals(raw, encoded);
        assertTrue(PasswordUtil.matches(raw, encoded));
        assertFalse(PasswordUtil.matches("wrong", encoded));
    }
}
