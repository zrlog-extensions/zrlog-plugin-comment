package com.zrlog.plugin.comment.config;

public class CommentHistoryRecord {

    private String time;
    private boolean success;
    private int count;
    private String message;

    public CommentHistoryRecord() {
    }

    public static CommentHistoryRecord create(String time, boolean success, int count, String message) {
        CommentHistoryRecord record = new CommentHistoryRecord();
        record.setTime(time);
        record.setSuccess(success);
        record.setCount(count);
        record.setMessage(message);
        return record;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
