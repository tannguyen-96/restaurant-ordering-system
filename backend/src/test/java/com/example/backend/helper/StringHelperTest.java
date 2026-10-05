package com.example.backend.helper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringHelperTest {

    @Test
    void defaultValueReturnsFallbackForNull() {
        assertThat(StringHelper.defaultValue(null, "fallback")).isEqualTo("fallback");
    }

    @Test
    void defaultValueReturnsFallbackForBlankValue() {
        assertThat(StringHelper.defaultValue("  ", "fallback")).isEqualTo("fallback");
    }

    @Test
    void defaultValueReturnsProvidedValueWhenNonBlank() {
        assertThat(StringHelper.defaultValue("provided", "fallback")).isEqualTo("provided");
    }
}
