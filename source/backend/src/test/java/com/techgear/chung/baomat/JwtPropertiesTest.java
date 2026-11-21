package com.techgear.chung.baomat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtPropertiesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void thieuHoacNganKhoaBiMatThiKhongHopLe() {
        assertEquals(1, validator.validate(new JwtProperties("", Duration.ofHours(24))).size());
        assertEquals(1, validator.validate(new JwtProperties("ngan", Duration.ofHours(24))).size());
    }

    @Test
    void khoaDu32KyTuThiHopLe() {
        assertTrue(validator.validate(new JwtProperties("x".repeat(32), Duration.ofHours(24))).isEmpty());
    }
}
