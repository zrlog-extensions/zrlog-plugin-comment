package com.zrlog.plugin.comment.config;

public class CommentApiResponse {

    private boolean success;
    public String message;
    public Object data;

    public static CommentApiResponse error(String message) {
        CommentApiResponse response = new CommentApiResponse(false);
        response.message = message;
        return response;
    }

    public static CommentApiResponse data(Object data) {
        CommentApiResponse response = success();
        response.data = data;
        return response;
    }

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
