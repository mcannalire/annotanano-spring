package com.annotanano;

import java.util.List;

public class Game {

	private String name;
	private String platform;
	private Integer percentComp;
	private String id;
	private String comment;
	private String logo;
	private Integer rating;
	private Integer hours;
	private List<GameCollection> collection;
	private Boolean col;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	
	public Integer getPercentComp() {
		return percentComp;
	}
	public void setPercentComp(Integer percentComp) {
		this.percentComp = percentComp;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public Integer getRating() {
		return rating;
	}
	public void setRating(Integer rating) {
		this.rating = rating;
	}
	public Integer getHours() {
		return hours;
	}
	public void setHours(Integer hours) {
		this.hours = hours;
	}
	public List<GameCollection> getCollection() {
		return collection;
	}
	public void setCollection(List<GameCollection> collection) {
		this.collection = collection;
	}
	public Boolean getCol() {
		return col;
	}
	public void setCol(Boolean col) {
		this.col = col;
	}
	
	
	
}
