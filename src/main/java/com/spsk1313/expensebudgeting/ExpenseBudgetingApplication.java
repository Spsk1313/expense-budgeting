package com.spsk1313.expensebudgeting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ExpenseBudgetingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseBudgetingApplication.class, args);
    }

}
