package com.fzb.zrlog.plugin.changyan.response;

import java.util.*;

public class ChangyanComment {
	private Integer sourceid;
	private String metadata;
	private List<CommentsEntry> comments;
	private Long ttime;
	private String title;
	private String url;

	public Integer getSourceid() {
		return sourceid;
	}

	public void setSourceid(Integer sourceid) {
		this.sourceid = sourceid;
	}

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	public List<CommentsEntry> getComments() {
		return comments;
	}

	public void setComments(List<CommentsEntry> comments) {
		this.comments = comments;
	}

	public Long getTtime() {
		return ttime;
	}

	public void setTtime(Long ttime) {
		this.ttime = ttime;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
