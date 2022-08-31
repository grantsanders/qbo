package com.fx.qbo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.google.gson.Gson;
import com.intuit.ipp.util.Config;

@SpringBootApplication
public class QboApplication {

	public static void main(String[] args) {
		ClientGUI main = new ClientGUI();
		
		SpringApplication.run(QboApplication.class, args);
	}
}
