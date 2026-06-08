package com.ukg.telestaff.sdlc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TelestaffSdlcPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelestaffSdlcPortalApplication.class, args);
    }
}
