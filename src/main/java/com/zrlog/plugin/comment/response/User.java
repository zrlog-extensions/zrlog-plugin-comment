package com.zrlog.plugin.comment.response;

public class User {
	private String usericon;
	private Long sohuPlusId;
	private String nickname;

	public String getUsericon() {
		return usericon;
	}

	public void setUsericon(String usericon) {
		this.usericon = usericon;
	}

	public Long getSohuPlusId() {
		return sohuPlusId;
	}

	public void setSohuPlusId(Long sohuPlusId) {
		this.sohuPlusId = sohuPlusId;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
}
