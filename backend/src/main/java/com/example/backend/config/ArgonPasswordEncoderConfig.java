package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ArgonPasswordEncoderConfig {

    @Bean
    // @Profile("local")
    public PasswordEncoder localPasswordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    // @Bean
    // @Profile("!local")
    // public PasswordEncoder productionPasswordEncoder() {
    //     return new Argon2PasswordEncoder(16, 32, 1, 19456, 2);
    // }
}
