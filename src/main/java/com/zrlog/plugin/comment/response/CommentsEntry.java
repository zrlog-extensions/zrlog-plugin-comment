package com.zrlog.plugin.comment.response;


import java.util.List;

public class CommentsEntry {
    private Long spcount;
    private Long cmtid;
    private Long apptype;
    private String ip;
    private String useragent;
    private String content;
    private Long score;
    private List<Object> attachment;
    private Long opcount;
    private Long referid;
    private Long channeltype;
    private Long replyid;
    private Long ctime;
    private Long from;
    private User user;
    private Long channelid;
    private Long status;

    public Long getSpcount() {
        return spcount;
    }

    public void setSpcount(Long spcount) {
        this.spcount = spcount;
    }

    public Long getCmtid() {
        return cmtid;
    }

    public void setCmtid(Long cmtid) {
        this.cmtid = cmtid;
    }

    public Long getApptype() {
        return apptype;
    }

    public void setApptype(Long apptype) {
        this.apptype = apptype;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUseragent() {
        return useragent;
    }

    public void setUseragent(String useragent) {
        this.useragent = useragent;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public List<Object> getAttachment() {
        return attachment;
    }

    public void setAttachment(List<Object> attachment) {
        this.attachment = attachment;
    }

    public Long getOpcount() {
        return opcount;
    }

    public void setOpcount(Long opcount) {
        this.opcount = opcount;
    }

    public Long getReferid() {
        return referid;
    }

    public void setReferid(Long referid) {
        this.referid = referid;
    }

    public Long getChanneltype() {
        return channeltype;
    }

    public void setChanneltype(Long channeltype) {
        this.channeltype = channeltype;
    }

    public Long getReplyid() {
        return replyid;
    }

    public void setReplyid(Long replyid) {
        this.replyid = replyid;
    }

    public Long getCtime() {
        return ctime;
    }

    public void setCtime(Long ctime) {
        this.ctime = ctime;
    }

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getChannelid() {
        return channelid;
    }

    public void setChannelid(Long channelid) {
        this.channelid = channelid;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }
}
