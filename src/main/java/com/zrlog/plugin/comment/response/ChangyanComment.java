package com.zrlog.plugin.comment.response;

import java.util.*;

public class ChangyanComment {
	private Long sourceid;
	private String metadata;
	private List<CommentsEntry> comments;
	private Long ttime;
	private String title;
	private String url;

	public Long getSourceid() {
		return sourceid;
	}

	public void setSourceid(Long sourceid) {
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
