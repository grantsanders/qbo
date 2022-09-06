package com.fx.qbo;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.Line;

@SpringBootApplication
public class QboApplication {

	public static void main(String[] args) {
		FileHandler handler = new FileHandler("bruh");

		handler.formatData();


		

		// ClientGUI main = new ClientGUI();
		
		// SpringApplication.run(QboApplication.class, args);
	}
}
