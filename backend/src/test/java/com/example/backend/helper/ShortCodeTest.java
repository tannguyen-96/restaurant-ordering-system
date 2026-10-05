package com.example.backend.helper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShortCodeTest {

    @Test
    void generateReturnsCodeWithRequestedLengthAndAllowedCharacters() {
        String code = ShortCode.generate(32);

        assertThat(code)
                .hasSize(32)
                .matches("[A-Za-z0-9]+");
    }

    @Test
    void generateWithZeroLengthReturnsEmptyCode() {
        assertThat(ShortCode.generate(0)).isEmpty();
    }
}
