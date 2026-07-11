package com.blogforge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.module.SimpleModule;

@Configuration
public class JacksonConfig {

    @Bean
    public SimpleModule stringTrimModule() {
        SimpleModule s = new SimpleModule("StringTrimModule");
        s.addDeserializer(String.class, new StringTrimDeserializer());
        return s;
    }
}
