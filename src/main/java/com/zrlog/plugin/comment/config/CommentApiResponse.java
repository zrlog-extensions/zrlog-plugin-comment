package com.zrlog.plugin.comment.config;

public class CommentApiResponse {

    private boolean success;

    public CommentApiResponse() {
    }

    private CommentApiResponse(boolean success) {
        this.success = success;
    }

    public static CommentApiResponse success() {
        return new CommentApiResponse(true);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
