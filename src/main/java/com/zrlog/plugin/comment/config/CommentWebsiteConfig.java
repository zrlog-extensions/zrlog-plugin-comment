package com.zrlog.plugin.comment.config;

import com.google.gson.Gson;

public class CommentWebsiteConfig {

    private String changyan;
    private String base;
    private String commentEmailNotify;
    private String type;
    private String syncHistory;
    private String userName;
    private Object userId;
    private String fullUrl;

    public void normalize(String defaultCallbackUrl) {
        Gson gson = new Gson();
        if (isBlank(changyan)) {
            ChangyanConfig changyanConfig = new ChangyanConfig();
            changyanConfig.setCallbackUrl(defaultCallbackUrl);
            changyan = gson.toJson(changyanConfig);
        }
        if (isBlank(base)) {
            CommentBaseConfig baseConfig = new CommentBaseConfig();
            baseConfig.setStyleStr("");
            base = gson.toJson(baseConfig);
        }
        if (isBlank(type)) {
            type = "base";
        }
    }

    public String normalizedType() {
        return isBlank(type) ? "base" : type;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public String getChangyan() {
        return changyan;
    }

    public void setChangyan(String changyan) {
        this.changyan = changyan;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getCommentEmailNotify() {
        return commentEmailNotify;
    }

    public void setCommentEmailNotify(String commentEmailNotify) {
        this.commentEmailNotify = commentEmailNotify;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSyncHistory() {
        return syncHistory;
    }

    public void setSyncHistory(String syncHistory) {
        this.syncHistory = syncHistory;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Object getUserId() {
        return userId;
    }

    public void setUserId(Object userId) {
        this.userId = userId;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }
}
