package com.fzb.zrlog.plugin.changyan.response;

public class ResponseEntry {
    private Long date;
    private Long log_id;
    private Long user_id;
    private Object meta;
    private Long site_id;
    private String action;

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public Long getLog_id() {
        return log_id;
    }

    public void setLog_id(Long log_id) {
        this.log_id = log_id;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public Object getMeta() {
        return meta;
    }

    public void setMeta(Object meta) {
        this.meta = meta;
    }

    public Long getSite_id() {
        return site_id;
    }

    public void setSite_id(Long site_id) {
        this.site_id = site_id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
