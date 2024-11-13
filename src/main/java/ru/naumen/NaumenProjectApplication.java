package ru.naumen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class NaumenProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(NaumenProjectApplication.class, args);
    }

}
