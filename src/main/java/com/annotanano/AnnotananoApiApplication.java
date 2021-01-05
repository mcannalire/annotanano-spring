package com.annotanano;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Year;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mongodb.Block;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@RestController
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class AnnotananoApiApplication {
	
	@PostMapping
	public User login(@RequestBody UserCredential uCred) {
		User user = new User();
		
		com.mongodb.MongoClient mongoClient = getMongoDb();
		MongoDatabase db = mongoClient.getDatabase("annotananodb");
		
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
		mongoClient.close();
		return user;
	}
	
	@SuppressWarnings("deprecation")
	@PostMapping("/getUserGames")
	public UserGames getUserGames(@RequestBody String userId) {
		com.mongodb.MongoClient mongoClient = getMongoDb();
		MongoDatabase db = mongoClient.getDatabase("annotananodb");
		
		MongoCollection<Document> collection = db.getCollection("gamers");
		
		Bson condition = new Document("$eq", userId);
		Bson filter = new Document("userId", condition);
		
		Document document = collection.find(filter).first();
		        
        UserGames user = new UserGames();
        user.setName(document.getString("name"));
        user.setUserId(document.getString("userId"));
        user.setAvatarUrl(document.getString("avatarUrl"));
        List<Document> listGamesDoc = (List<Document>)document.get("gamesThisYear");
        List<Game> userGames = new ArrayList<Game>();
        listGamesDoc.forEach((Document d) -> {
        	Game game = new Game();
        	game.setName(d.getString("name"));
        	game.setPercentComp(d.getInteger("percentComp"));
        	game.setPlatform(d.getString("platform"));
        	game.setId(d.getString("id"));
        	game.setHours(d.getInteger("hours"));
        	game.setComment(d.getString("comment"));
        	game.setLogo(d.getString("logo"));
        	game.setRating(d.getInteger("rating"));
        	game.setCol(d.getBoolean("col"));
        	
        	if(d.get("collection") != null) {
        		List<Document> listGamesCollection = (List<Document>)d.get("collection");
        		List<GameCollection> listCollectionToSave = new ArrayList<GameCollection>();
        		listGamesCollection.forEach((Document dd) -> {
        			GameCollection gc = new GameCollection();
            		gc.setName(dd.getString("name"));
            		gc.setPercentComp(dd.getInteger("percentComp"));
            		listCollectionToSave.add(gc);
        		});
        		game.setCollection(listCollectionToSave);
        	}
        	userGames.add(game);
        });
        user.setGamesThisYear(userGames);
        
        List<Document> listMovieDoc = (List<Document>)document.get("moviesThisYear");
        List<Movie> userMovies = new ArrayList<Movie>();
        if(listMovieDoc != null) {
        	listMovieDoc.forEach((Document d) -> {
            	Movie movie = new Movie();
            	movie.setName(d.getString("name"));
            	movie.setId(d.getString("id"));
            	movie.setComment(d.getString("comment"));
            	movie.setUrl(d.getString("url"));
            	movie.setRating(d.getInteger("rating"));
            	
            	
            	userMovies.add(movie);
            });
            user.setMoviesThisYear(userMovies);
        }
        
        List<Document> listSeriesDoc = (List<Document>)document.get("seriesThisYear");
        List<TvSeries> userSeries = new ArrayList<TvSeries>();
        if(listSeriesDoc != null) {
        	listMovieDoc.forEach((Document d) -> {
            	TvSeries serie = new TvSeries();
            	serie.setName(d.getString("name"));
            	serie.setId(d.getString("id"));
            	serie.setComment(d.getString("comment"));
            	serie.setUrl(d.getString("url"));
            	serie.setRating(d.getInteger("rating"));
            	
            	userSeries.add(serie);
            });
            user.setSeriesThisYear(userSeries);
        }
	        
        mongoClient.close();
		return user;
	}
	
	@PutMapping
	public UserGames update(@RequestBody UserGames userGames) {
		
		com.mongodb.MongoClient mongoClient = getMongoDb();
		MongoDatabase db = mongoClient.getDatabase("annotananodb");
		
		MongoCollection<Document> collection = db.getCollection("gamers");
		Bson condition = new Document("$eq", userGames.getUserId());
		Bson filter = new Document("userId", condition);
		
		Document query = collection.find(filter).first();
		Bson dupdate = null;
		if(query != null) {
			if(userGames != null) {
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
					
					List<Document> documentListGames = new ArrayList<Document>();
					for (Game game : userGames.getGamesThisYear()) {
						Document gameDocument = new Document();
						gameDocument.append("id", game.getId());
						gameDocument.append("name", game.getName());
						gameDocument.append("percentComp", game.getPercentComp());
						gameDocument.append("platform", game.getPlatform());
						
						gameDocument.append("hours", game.getHours());
						gameDocument.append("comment", game.getComment());
						gameDocument.append("logo", game.getLogo());
						gameDocument.append("rating", game.getRating());
						gameDocument.append("col", game.getCol());
						
						if(game.getCollection() != null && !game.getCollection().isEmpty()) {
							List<Document> documentListCollection = new ArrayList<Document>();
							for (GameCollection gc : game.getCollection()) {
								Document gcDocument = new Document();
								gcDocument.append("name", gc.getName());
								gcDocument.append("percentComp", gc.getPercentComp());
								documentListCollection.add(gcDocument);
							}
							gameDocument.append("collection", documentListCollection);
						}
						documentListGames.add(gameDocument);
					}
					
					update.append("gamesThisYear", documentListGames);
				}
			
			
				if(userGames.getMoviesThisYear() != null && !userGames.getMoviesThisYear().isEmpty()) {
					for (Movie movie : userGames.getMoviesThisYear()) {
						if(movie.getId() == null || movie.getId().isEmpty()) {
							ObjectId id = new ObjectId();
							movie.setId(id.get().toString());
						}
					}
					
					List<Document> documentListMovies = new ArrayList<Document>();
					for (Movie movie : userGames.getMoviesThisYear()) {
						Document movieDocument = new Document();
						movieDocument.append("id", movie.getId());
						movieDocument.append("name", movie.getName());						
						movieDocument.append("comment", movie.getComment());
						movieDocument.append("logo", movie.getUrl());
						movieDocument.append("rating", movie.getRating());
						documentListMovies.add(movieDocument);
					}
					
					update.append("moviesThisYear", documentListMovies);
				}
				
				if(userGames.getSeriesThisYear() != null && !userGames.getSeriesThisYear().isEmpty()) {
					for (TvSeries serie : userGames.getSeriesThisYear()) {
						if(serie.getId() == null || serie.getId().isEmpty()) {
							ObjectId id = new ObjectId();
							serie.setId(id.get().toString());
						}
					}
					
					List<Document> documentListSeries = new ArrayList<Document>();
					for (TvSeries serie : userGames.getSeriesThisYear()) {
						Document seriesDocument = new Document();
						seriesDocument.append("id", serie.getId());
						seriesDocument.append("name", serie.getName());						
						seriesDocument.append("comment", serie.getComment());
						seriesDocument.append("logo", serie.getUrl());
						seriesDocument.append("rating", serie.getRating());
						documentListSeries.add(seriesDocument);
					}
					
					update.append("seriesThisYear", documentListSeries);
				}
				
				dupdate = new Document("$set", update);
				collection.updateOne(query, dupdate);
			}
		} else {
			if(userGames != null){
				Document update = new Document();
				update.append("name", userGames.getName());
				update.append("avatarUrl", userGames.getAvatarUrl());
				update.append("userId", userGames.getUserId());
				if(userGames.getGamesThisYear() != null && !userGames.getGamesThisYear().isEmpty()) {
					for (Game game : userGames.getGamesThisYear()) {
						if(game.getId() == null || game.getId().isEmpty()) {
							ObjectId id = new ObjectId();
							game.setId(id.get().toString());
						}
					}
					
					List<Document> documentListGames = new ArrayList<Document>();
					for (Game game : userGames.getGamesThisYear()) {
						Document gameDocument = new Document();
						gameDocument.append("id", game.getId());
						gameDocument.append("name", game.getName());
						gameDocument.append("percentComp", game.getPercentComp());
						gameDocument.append("platform", game.getPlatform());
						
						gameDocument.append("hours", game.getHours());
						gameDocument.append("comment", game.getComment());
						gameDocument.append("logo", game.getLogo());
						gameDocument.append("rating", game.getRating());
						gameDocument.append("col", game.getCol());
						
						if(game.getCollection() != null && !game.getCollection().isEmpty()) {
							List<Document> documentListCollection = new ArrayList<Document>();
							for (GameCollection gc : game.getCollection()) {
								Document gcDocument = new Document();
								gcDocument.append("name", gc.getName());
								gcDocument.append("percentComp", gc.getPercentComp());
								documentListCollection.add(gcDocument);
							}
							gameDocument.append("collection", documentListCollection);
						}
						
						documentListGames.add(gameDocument);
					}
					
					update.append("gamesThisYear", documentListGames);
					//Bson dupdate = new Document("$set", update);
				}
				
				if(userGames.getMoviesThisYear() != null && !userGames.getMoviesThisYear().isEmpty()) {
					for (Movie movie : userGames.getMoviesThisYear()) {
						if(movie.getId() == null || movie.getId().isEmpty()) {
							ObjectId id = new ObjectId();
							movie.setId(id.get().toString());
						}
					}
					
					List<Document> documentListMovies = new ArrayList<Document>();
					for (Movie movie : userGames.getMoviesThisYear()) {
						Document movieDocument = new Document();
						movieDocument.append("id", movie.getId());
						movieDocument.append("name", movie.getName());						
						movieDocument.append("comment", movie.getComment());
						movieDocument.append("logo", movie.getUrl());
						movieDocument.append("rating", movie.getRating());
						documentListMovies.add(movieDocument);
					}
					
					update.append("moviesThisYear", documentListMovies);
				}
				
				if(userGames.getSeriesThisYear() != null && !userGames.getSeriesThisYear().isEmpty()) {
					for (TvSeries serie : userGames.getSeriesThisYear()) {
						if(serie.getId() == null || serie.getId().isEmpty()) {
							ObjectId id = new ObjectId();
							serie.setId(id.get().toString());
						}
					}
					
					List<Document> documentListSeries = new ArrayList<Document>();
					for (TvSeries serie : userGames.getSeriesThisYear()) {
						Document seriesDocument = new Document();
						seriesDocument.append("id", serie.getId());
						seriesDocument.append("name", serie.getName());						
						seriesDocument.append("comment", serie.getComment());
						seriesDocument.append("logo", serie.getUrl());
						seriesDocument.append("rating", serie.getRating());
						documentListSeries.add(seriesDocument);
					}
					
					update.append("seriesThisYear", documentListSeries);
				}
				collection.insertOne(update);
			}
			
		}
		
		mongoClient.close();
		return userGames;
	}
	
	@SuppressWarnings("deprecation")
	@GetMapping("/getAll")
	public List<UserGames> getAll() {
		com.mongodb.MongoClient mongoClient = getMongoDb();
		MongoDatabase db = mongoClient.getDatabase("annotananodb");
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
	            	game.setHours(d.getInteger("hours"));
	            	game.setComment(d.getString("comment"));
	            	game.setLogo(d.getString("logo"));
	            	game.setRating(d.getInteger("rating"));
	            	game.setCol(d.getBoolean("col"));
	            	
	            	if(d.get("collection") != null) {
	            		List<Document> listGamesCollection = (List<Document>)d.get("collection");
	            		List<GameCollection> listCollectionToSave = new ArrayList<GameCollection>();
	            		listGamesCollection.forEach((Document dd) -> {
	            			GameCollection gc = new GameCollection();
	                		gc.setName(dd.getString("name"));
	                		gc.setPercentComp(dd.getInteger("percentComp"));
	                		listCollectionToSave.add(gc);
	            		});
	            		game.setCollection(listCollectionToSave);
	            	}
	            	
	            	userGames.add(game);
	            });
	            user.setGamesThisYear(userGames);
	            
	            List<Movie> userMovies = new ArrayList<Movie>();
	            List<Document> listMoviesDoc = (List<Document>)document.get("moviesThisYear");
	            listMoviesDoc.forEach((Document d) -> {
	            	Movie movie = new Movie();
	            	movie.setName(d.getString("name"));
	            	movie.setId(d.getString("id"));
	            	movie.setComment(d.getString("comment"));
	            	movie.setUrl(d.getString("url"));
	            	movie.setRating(d.getInteger("rating"));
	            	
	            	userMovies.add(movie);
	            });
	            user.setMoviesThisYear(userMovies);
	            
	            List<TvSeries> userSeries = new ArrayList<TvSeries>();
	            List<Document> listSeriesDoc = (List<Document>)document.get("seriesThisYear");
	            listSeriesDoc.forEach((Document d) -> {
	            	TvSeries serie = new TvSeries();
	            	serie.setName(d.getString("name"));
	            	serie.setId(d.getString("id"));
	            	serie.setComment(d.getString("comment"));
	            	serie.setUrl(d.getString("url"));
	            	serie.setRating(d.getInteger("rating"));
	            	
	            	userSeries.add(serie);
	            });
	            user.setSeriesThisYear(userSeries);
	            uGames.add(user);
	        }
	   });
		mongoClient.close();
		return uGames;
	}
	
	@SuppressWarnings("deprecation")
	@GetMapping("/getAllGoldBook")
	public List<UserGoldBook> getAllGoldbook() {
		com.mongodb.MongoClient mongoClient = getMongoDb();
		MongoDatabase db = mongoClient.getDatabase("annotananodb");
		MongoCollection<Document> collection = db.getCollection("goldbook");
		
		List<UserGoldBook> uGames = new ArrayList<UserGoldBook>();
		
		FindIterable<Document> cursor = collection.find();
		
		cursor.forEach(new Block<Document>() {
	        @Override
	        public void apply(final Document document) {
	            UserGoldBook user = new UserGoldBook();
	            user.setName(document.getString("name"));
	            user.setAvatarUrl(document.getString("avatarUrl"));
	            List<String> yearAsList = getYearList();
	            Map<String, List<Game>> gameGoldbook = new HashMap<String, List<Game>>();
	            Map<String, List<Movie>> movieGoldbook = new HashMap<String, List<Movie>>();
	            Map<String, List<TvSeries>> seriesGoldbook = new HashMap<String, List<TvSeries>>();
	            for (String year : yearAsList) {
	            	List<Document> listGamesDoc = (List<Document>)document.get(year+"_list");
		            
		            if(listGamesDoc != null) {
		            	listGamesDoc.forEach((Document d) -> {
			            	Game game = new Game();
			            	game.setName(d.getString("name"));
			            	game.setPercentComp(d.getInteger("percentComp"));
			            	game.setPlatform(d.getString("platform"));
			            	game.setId(d.getString("id"));
			            	game.setHours(d.getInteger("hours"));
			            	game.setComment(d.getString("comment"));
			            	game.setLogo(d.getString("logo"));
			            	game.setRating(d.getInteger("rating"));
			            	game.setCol(d.getBoolean("col"));
			            	
			            	if(d.get("collection") != null) {
			            		List<Document> listGamesCollection = (List<Document>)d.get("collection");
			            		List<GameCollection> listCollectionToSave = new ArrayList<GameCollection>();
			            		listGamesCollection.forEach((Document dd) -> {
			            			GameCollection gc = new GameCollection();
			                		gc.setName(dd.getString("name"));
			                		gc.setPercentComp(dd.getInteger("percentComp"));
			                		listCollectionToSave.add(gc);
			            		});
			            		game.setCollection(listCollectionToSave);
			            	}
			            	
			            	//userGames.add(game);
			            	if(gameGoldbook.get(year+"_list") == null) {
			            		List<Game> userGames = new ArrayList<Game>();
			            		gameGoldbook.put(year+"_list", userGames);
			            	} else {
			            		gameGoldbook.get(year+"_list").add(game);
			            	}
			            });
			            
		            }
		            
		            //MOVIES GOLDBOOK
		            List<Document> listMoviesDoc = (List<Document>)document.get(year+"_movies_list");
		            
		            if(listMoviesDoc != null) {
		            	listMoviesDoc.forEach((Document d) -> {
		            		Movie movie = new Movie();
		            		movie.setName(d.getString("name"));
		            		movie.setId(d.getString("id"));
			            	movie.setComment(d.getString("comment"));
			            	movie.setUrl(d.getString("url"));
			            	movie.setRating(d.getInteger("rating"));
			            	
			            	if(movieGoldbook.get(year+"_movies_list") == null) {
			            		List<Movie> userMovies = new ArrayList<Movie>();
			            		movieGoldbook.put(year+"_movies_list", userMovies);
			            	} else {
			            		movieGoldbook.get(year+"_movies_list").add(movie);
			            	}
		            	});
		            }
		            
		            //SERIES TV GOLDBOOK
		            List<Document> listSeriesDoc = (List<Document>)document.get(year+"_series_list");
		            
		            if(listSeriesDoc != null) {
		            	listSeriesDoc.forEach((Document d) -> {
		            		TvSeries serie = new TvSeries();
		            		serie.setName(d.getString("name"));
		            		serie.setId(d.getString("id"));
		            		serie.setComment(d.getString("comment"));
		            		serie.setUrl(d.getString("url"));
		            		serie.setRating(d.getInteger("rating"));
			            	
			            	if(seriesGoldbook.get(year+"_series_list") == null) {
			            		List<TvSeries> userSeries = new ArrayList<TvSeries>();
			            		seriesGoldbook.put(year+"_series_list", userSeries);
			            	} else {
			            		seriesGoldbook.get(year+"_series_list").add(serie);
			            	}
		            	});
		            }
		            
				}
	            user.setUserGoldbook(gameGoldbook);
	            user.setUserMovieGoldbook(movieGoldbook);
	            user.setUserSeriesGoldbook(seriesGoldbook);
	            uGames.add(user);
	            
	        }
	   });
		mongoClient.close();
		return uGames;
	}
	
	private static List<String> getYearList(){
		long i = 1;
		List<String> yearAsStringArray = new ArrayList<String>();
		while(Year.now().minusYears(i).getValue() >= 2020) {
			yearAsStringArray.add(String.valueOf(Year.now().minusYears(i).getValue()));
			i++;
		}
		
		return yearAsStringArray;
	}
	
	@SuppressWarnings("deprecation")
	@PostMapping("/getAllByUserId")
	public List<UserGames> getAllByUserId(@RequestBody String userId) {
		com.mongodb.MongoClient mongoClient = getMongoDb();
		MongoDatabase db = mongoClient.getDatabase("annotananodb");
		MongoCollection<Document> collection = db.getCollection("gamers");
		
		List<UserGames> uGames = new ArrayList<UserGames>();
		
		FindIterable<Document> cursor = collection.find();
		
		cursor.forEach(new Block<Document>() {
	        @Override
	        public void apply(final Document document) {
	            UserGames user = new UserGames();
	            user.setName(document.getString("name"));
	            user.setAvatarUrl(document.getString("avatarUrl"));
	            String documentUserId = document.getString("userId");
	            if(userId != null && documentUserId != null && documentUserId.equals(userId)) {
	            	user.setUserId(documentUserId);
	            }
	            
	            List<Document> listGamesDoc = (List<Document>)document.get("gamesThisYear");
	            List<Game> userGames = new ArrayList<Game>();
	            listGamesDoc.forEach((Document d) -> {
	            	Game game = new Game();
	            	game.setName(d.getString("name"));
	            	game.setPercentComp(d.getInteger("percentComp"));
	            	game.setPlatform(d.getString("platform"));
	            	game.setId(d.getString("id"));
	            	game.setHours(d.getInteger("hours"));
	            	game.setComment(d.getString("comment"));
	            	game.setLogo(d.getString("logo"));
	            	game.setRating(d.getInteger("rating"));
	            	game.setCol(d.getBoolean("col"));
	            	
	            	if(d.get("collection") != null) {
	            		List<Document> listGamesCollection = (List<Document>)d.get("collection");
	            		List<GameCollection> listCollectionToSave = new ArrayList<GameCollection>();
	            		listGamesCollection.forEach((Document dd) -> {
	            			GameCollection gc = new GameCollection();
	                		gc.setName(dd.getString("name"));
	                		gc.setPercentComp(dd.getInteger("percentComp"));
	                		listCollectionToSave.add(gc);
	            		});
	            		game.setCollection(listCollectionToSave);
	            	}
	            	
	            	userGames.add(game);
	            });
	            user.setGamesThisYear(userGames);
	            
	            List<Movie> userMovies = new ArrayList<Movie>();
	            List<Document> listMoviesDoc = (List<Document>)document.get("moviesThisYear");
	            listMoviesDoc.forEach((Document d) -> {
	            	Movie movie = new Movie();
	            	movie.setName(d.getString("name"));
	            	movie.setId(d.getString("id"));
	            	movie.setComment(d.getString("comment"));
	            	movie.setUrl(d.getString("url"));
	            	movie.setRating(d.getInteger("rating"));
	            	
	            	userMovies.add(movie);
	            });
	            user.setMoviesThisYear(userMovies);
	            
	            List<TvSeries> userSeries = new ArrayList<TvSeries>();
	            List<Document> listSeriesDoc = (List<Document>)document.get("seriesThisYear");
	            listSeriesDoc.forEach((Document d) -> {
	            	TvSeries serie = new TvSeries();
	            	serie.setName(d.getString("name"));
	            	serie.setId(d.getString("id"));
	            	serie.setComment(d.getString("comment"));
	            	serie.setUrl(d.getString("url"));
	            	serie.setRating(d.getInteger("rating"));
	            	
	            	userSeries.add(serie);
	            });
	            user.setSeriesThisYear(userSeries);
	            uGames.add(user);
	        }
	   });
		mongoClient.close();
		return uGames;
	}


	public static void main(String[] args) {
		SpringApplication.run(AnnotananoApiApplication.class, args);
	}
	
	private static Integer getValueFromJson(String key, String json) {
		Pattern p = Pattern.compile("\""+key+"\":(\\d+)");
		Matcher m = p.matcher(json);
		if(m.find()) {
			p = Pattern.compile("(\\d+)");
			m = p.matcher(m.group());
			return m.find() ? Integer.valueOf(m.group()) : null;
		}
		return null;
	}
	
	private static String jsonFormatter(String json) {
		json = json.replaceAll("\"\"", "\"").replaceAll("\\s","");
		return json;
	}
	
	private com.mongodb.MongoClient getMongoDb() {
		MongoClientURI uri = new MongoClientURI(
			    "mongodb+srv://lokad90:mongodb@cluster0-biuot.mongodb.net/test?retryWrites=true&w=majority");
			
		com.mongodb.MongoClient mongoClient = new com.mongodb.MongoClient(uri);
		//MongoDatabase database = mongoClient.getDatabase("annotananodb");
		return mongoClient;
	}
	
	/*@SuppressWarnings("deprecation")
	@Scheduled(cron = "0 0 4 ? * * *", zone = "Europe/Paris")
	public void scheduledGoldBook() {*/
		/*com.mongodb.MongoClient mongoClient = getMongoDb();
		MongoDatabase db = mongoClient.getDatabase("annotananodb");
		
		//get gamers profiles
		MongoCollection<Document> gamers = db.getCollection("gamers");
		//get goldbook infos
		MongoCollection<Document> goldbook = db.getCollection("goldbook");
		
		FindIterable<Document> gamersCursor = gamers.find();
		FindIterable<Document> goldbookCursor = goldbook.find();
		
		Map<String, Integer> mapYearGamesPlayed = new HashMap<String, Integer>();
		
		gamersCursor.forEach(new Block<Document>() {
			@Override
	        public void apply(final Document gamerDocument) {
				String gamerUserId = gamerDocument.getString("userId");
				boolean userFound = false;
				goldbookCursor.forEach(new Block<Document>() {
					@Override
			        public void apply(final Document goldbookDocument) {
						String goldbookUserId = goldbookDocument.getString("userId");
						if(goldbookUserId.equals(gamerUserId)) {
							userFound = true;
							goldbookDocument.append("name", gamerDocument.getString("name"));
							goldbookDocument.append("avatarUrl",gamerDocument.getString("avatarUrl"));
							
							List<Document> listGamesDoc = (List<Document>)gamerDocument.get("gamesThisYear");
							int year = Year.now(ZoneId.of("Europe/Paris")).getValue();
							
							goldbookDocument.append(year + "_list", listGamesDoc);	
						}
					}
				});
				if(!userFound) {
					goldbookDocument.append("name", gamerDocument.getString("name"));
					goldbookDocument.append("avatarUrl",gamerDocument.getString("avatarUrl"));
				}
			}
		});
		
		goldbook.updateOne(filter, update);
		
		/*Map<String, Integer> userIdChart = new HashMap<String, Integer>();
		for (String yearUserId : mapYearGamesPlayed.keySet()) {
			int numerOfGamesOuter = mapYearGamesPlayed.get(yearUserId);
			for (String yui : mapYearGamesPlayed.keySet()) {
				int numerOfGamesInner = mapYearGamesPlayed.get(yui);
				if(numerOfGamesInner > numerOfGamesOuter) {
					userIdChart.put();
				}
			}
		}*/
	//}
	
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
