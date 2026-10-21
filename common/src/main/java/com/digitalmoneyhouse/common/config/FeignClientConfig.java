package com.digitalmoneyhouse.common.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    @Bean
    ErrorDecoder feignErrorDecoder() {
        return new FeignErrorDecoder();
    }
}
