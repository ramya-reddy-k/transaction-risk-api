package com.ramya.transactionrisk;

import org.springframework.boot.SpringApplication;

public class TestTransactionRiskApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(TransactionRiskApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
