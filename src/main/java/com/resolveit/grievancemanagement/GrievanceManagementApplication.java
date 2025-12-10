package com.resolveit.grievancemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GrievanceManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrievanceManagementApplication.class, args);
    }

}
