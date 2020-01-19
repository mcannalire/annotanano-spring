package com.annotanano;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.mongodb.Block;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@SpringBootApplication
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
	@PostMapping("/getUserGames")
	public UserGames getUserGames(@RequestBody String userId) {
		MongoDatabase db = getMongoDb();
		MongoCollection<Document> collection = db.getCollection("gamers");
		
		Document document = collection.find().first();
		        
        UserGames user = new UserGames();
        user.setName(document.getString("name"));
        user.setAvatarUrl(document.getString("avatarUrl"));
        List<Document> listGamesDoc = (List<Document>)document.get("gamesThisYear");
        List<Game> userGames = new ArrayList<Game>();
        listGamesDoc.forEach((Document d) -> {
        	Game game = new Game();
        	game.setName(d.getString("name"));
        	game.setPercentComp(d.getInteger("percentComp"));
        	game.setPlatform(d.getString("platform"));
        	game.setId(d.getString("id"));
        	userGames.add(game);
        });
        user.setGamesThisYear(userGames);
	        
	   
		return user;
	}
	
	@PutMapping
	public UserGames update(@RequestBody UserGames userGames) {
		
		MongoDatabase db = getMongoDb();
		
		MongoCollection<Document> collection = db.getCollection("gamers");
		Bson condition = new Document("$eq", userGames.getUserId());
		Bson filter = new Document("userId", condition);
		
		Document query = collection.find(filter).first();
		
		if(query != null && userGames != null) {
			Document update = new Document();
			update.append("name", userGames.getName());
			update.append("avatarUrl", userGames.getAvatarUrl());
			if(userGames.getGamesThisYear() != null && !userGames.getGamesThisYear().isEmpty()) {
				for (Game game : userGames.getGamesThisYear()) {
					if(game.getId() == null || game.getId().isEmpty()) {
						ObjectId id = new ObjectId();
						game.setId(id.get().toString());
					}
				}
				
				update.append("gamesThisYear", userGames.getGamesThisYear());
				Bson dupdate = new Document("$set", update);
				collection.updateOne(query, dupdate);
			}
			
			
		}
		
		return userGames;
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
	            List<Document> listGamesDoc = (List<Document>)document.get("gamesThisYear");
	            List<Game> userGames = new ArrayList<Game>();
	            listGamesDoc.forEach((Document d) -> {
	            	Game game = new Game();
	            	game.setName(d.getString("name"));
	            	game.setPercentComp(d.getInteger("percentComp"));
	            	game.setPlatform(d.getString("platform"));
	            	game.setId(d.getString("id"));
	            	userGames.add(game);
	            });
	            user.setGamesThisYear(userGames);
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
	
	@Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*"); // this allows all origin
        config.addAllowedHeader("*"); // this allows all headers
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("HEAD");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

}
