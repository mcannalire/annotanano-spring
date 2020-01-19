package com.annotanano;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.Block;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@SpringBootApplication
@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class AnnotananoApiApplication {
	
	@PostMapping
	public User login(@RequestBody UserCredential uCred) {
		User user = new User();
		
		MongoDatabase db = getMongoDb();
		
		MongoCollection<Document> collection = db.getCollection("users");
		Bson condition = new Document("$eq", uCred.getUserName());
		Bson filter = new Document("userName", condition);
		
		Document document = collection.find(filter).first();
		
		if(document != null) {
			if(document.getString("pwd").equals(uCred.getPwd()))
				user.setUserId(document.getString("userId"));
			else
				user.setUserId("NA");
		} else {
			user.setUserId("NA");
		}
		
		return user;
	}
	
	@SuppressWarnings("deprecation")
	@GetMapping("/getAll")
	public List<UserGames> getAll() {
		MongoDatabase db = getMongoDb();
		MongoCollection<Document> collection = db.getCollection("gamers");
		
		List<UserGames> uGames = new ArrayList<UserGames>();
		
		FindIterable<Document> cursor = collection.find();
		
		cursor.forEach(new Block<Document>() {
	        @Override
	        public void apply(final Document document) {
	            UserGames user = new UserGames();
	            user.setName(document.getString("name"));
	            user.setAvatarUrl(document.getString("avatarUrl"));
	            user.setGamesThisYear(document.getList("gamesThisYear", Game.class));
	            uGames.add(user);
	        }
	   });
		return uGames;
	}

	public static void main(String[] args) {
		SpringApplication.run(AnnotananoApiApplication.class, args);
	}
	
	private MongoDatabase getMongoDb() {
		MongoClientURI uri = new MongoClientURI(
			    "mongodb+srv://lokad90:mongodb@cluster0-biuot.mongodb.net/test?retryWrites=true&w=majority");
			
		com.mongodb.MongoClient mongoClient = new com.mongodb.MongoClient(uri);
		MongoDatabase database = mongoClient.getDatabase("annotananodb");
		return database;
	}

}
