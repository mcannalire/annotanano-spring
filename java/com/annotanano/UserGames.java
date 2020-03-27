package com.annotanano;

import java.util.List;

public class UserGames {
	
	private String userId;
	private String name;
	private String avatarUrl;
	private List<Game> gamesThisYear;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAvatarUrl() {
		return avatarUrl;
	}
	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
	public List<Game> getGamesThisYear() {
		return gamesThisYear;
	}
	public void setGamesThisYear(List<Game> gamesThisYear) {
		this.gamesThisYear = gamesThisYear;
	}
	
	

}
