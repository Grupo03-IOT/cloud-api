package com.pe.cloudapi;

import com.pe.cloudapi.shared.infrastructure.configuration.BoundedContextBeanNameGenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication(nameGenerator = BoundedContextBeanNameGenerator.class)
@EnableFeignClients
public class CloudApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudApiApplication.class, args);
    }

}
