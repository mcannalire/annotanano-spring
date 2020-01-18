package com.annotanano;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class AnnotananoApiApplication {
	
	@PostMapping
	public String login(@RequestBody String userName) {
		return "LOGIN EFFETTUATO CON " + userName;
	}
	
	@GetMapping
	public String login() {
		return "LOGIN EFFETTUATO MARCO";
	}

	public static void main(String[] args) {
		SpringApplication.run(AnnotananoApiApplication.class, args);
	}

}
