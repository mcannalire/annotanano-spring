package com.annotanano;

import java.util.List;
import java.util.Map;

public class UserGoldBook extends UserGames{

	private Map<String, List<Game>> userGoldbook;
	private Map<String, List<Movie>> userMovieGoldbook;
	private Map<String, List<TvSeries>> userSeriesGoldbook;

	public Map<String, List<Movie>> getUserMovieGoldbook() {
		return userMovieGoldbook;
	}

	public void setUserMovieGoldbook(Map<String, List<Movie>> userMovieGoldbook) {
		this.userMovieGoldbook = userMovieGoldbook;
	}

	public Map<String, List<TvSeries>> getUserSeriesGoldbook() {
		return userSeriesGoldbook;
	}

	public void setUserSeriesGoldbook(Map<String, List<TvSeries>> userSeriesGoldbook) {
		this.userSeriesGoldbook = userSeriesGoldbook;
	}

	public Map<String, List<Game>> getUserGoldbook() {
		return userGoldbook;
	}

	public void setUserGoldbook(Map<String, List<Game>> userGoldbook) {
		this.userGoldbook = userGoldbook;
	}
}
