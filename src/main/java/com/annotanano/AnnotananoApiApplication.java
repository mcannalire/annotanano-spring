package com.annotanano;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

@SpringBootApplication
@RestController
@RequestMapping("api")
public class AnnotananoApiApplication {
	
	@PostMapping
	public String login(@RequestBody String userName) {

		return "LOGIN EFFETTUATO CON " + userName;
	}
	
	@GetMapping
	public String login() {
		/*MongoClientURI uri = new MongoClientURI(
			    "mongodb+srv://lokad90:mongodb@cluster0-biuot.mongodb.net/test?retryWrites=true&w=majority");
			
		com.mongodb.MongoClient mongoClient = new com.mongodb.MongoClient(uri);
		MongoDatabase database = mongoClient.getDatabase("test");*/ 
		
		
		//return "LOGIN EFFETTUATO MARCO, nome database mongo: " + database.getName();
		return "0000";
	}

	public static void main(String[] args) {
		SpringApplication.run(AnnotananoApiApplication.class, args);
	}

}
