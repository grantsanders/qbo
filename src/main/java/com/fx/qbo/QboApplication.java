package com.fx.qbo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QboApplication {

	public static void main(String[] args) {

		ClientGUI main = new ClientGUI();

		SpringApplication.run(QboApplication.class, args);
	}
}
