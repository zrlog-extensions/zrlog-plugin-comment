package com.zrlog.plugin.comment.config;

public class CommentUpdateRequest {

    private String type;
    private String commentEmailNotify;
    private String changyan;
    private String base;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCommentEmailNotify() {
        return commentEmailNotify;
    }

    public void setCommentEmailNotify(String commentEmailNotify) {
        this.commentEmailNotify = commentEmailNotify;
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
}
