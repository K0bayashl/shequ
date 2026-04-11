package com.community.mvp.backend;

import com.community.mvp.backend.config.CommunityMvpProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CommunityMvpProperties.class)
public class CommunityMvpBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityMvpBackendApplication.class, args);
    }
}

