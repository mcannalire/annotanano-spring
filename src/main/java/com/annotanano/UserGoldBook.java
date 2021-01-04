package com.annotanano;

import java.util.List;
import java.util.Map;

public class UserGoldBook extends UserGames{

	private Map<String, List<Game>> userGoldbook;

	public Map<String, List<Game>> getUserGoldbook() {
		return userGoldbook;
	}

	public void setUserGoldbook(Map<String, List<Game>> userGoldbook) {
		this.userGoldbook = userGoldbook;
	}
}
