package com.zrlog.plugin.comment.controller;

import com.google.gson.Gson;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.client.ClientActionHandler;
import com.zrlog.plugin.comment.config.ChangyanConfig;
import com.zrlog.plugin.comment.config.CommentWebsiteConfig;
import com.zrlog.plugin.comment.config.WebsiteKeyRequest;
import com.zrlog.plugin.comment.dao.CommentDAO;
import com.zrlog.plugin.comment.response.ChangyanComment;
import com.zrlog.plugin.comment.response.CommentsEntry;
import com.zrlog.plugin.common.IdUtil;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.common.model.Comment;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.data.codec.MsgPacket;
import com.zrlog.plugin.data.codec.MsgPacketStatus;
import com.zrlog.plugin.type.ActionType;

import java.net.URL;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.zrlog.plugin.comment.service.CommentService;

public class ChangyanController {

    private static final Logger LOGGER = LoggerUtil.getLogger(ChangyanController.class);

    private final IOSession session;
    private final MsgPacket requestPacket;
    private final HttpRequestInfo requestInfo;
    private final Gson gson = new Gson();

    public ChangyanController(IOSession session, MsgPacket requestPacket, HttpRequestInfo requestInfo) {
        this.session = session;
        this.requestPacket = requestPacket;
        this.requestInfo = requestInfo;
    }

    /**
     * 反向同步接口
     */
    public void sync() {
        session.sendJsonMsg(WebsiteKeyRequest.of("changyan"), ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, msgPacket -> {
            CommentWebsiteConfig websiteConfig = gson.fromJson(msgPacket.getDataStr(), CommentWebsiteConfig.class);
            ChangyanConfig changyan = changyanConfig(websiteConfig);
            String callbackUrl = changyan.getCallbackUrl();
            String ignoreChar = "/p/" + session.getPlugin().getShortName();
            try {
                if (callbackUrl != null && new URL(callbackUrl).getPath().replace(ignoreChar, "").equals(requestInfo.getUri().replace(".action", ""))) {
                    String commentJsonStr = requestInfo.getParam().get("data")[0];
                    LOGGER.info(commentJsonStr);
                    final ChangyanComment changyanComment = gson.fromJson(commentJsonStr, ChangyanComment.class);
                    dealSyncRequest(changyanComment);
                } else {
                    session.sendMsg(ContentType.HTML, ClientActionHandler.ACTION_NOT_FOUND_PAGE, requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "", e);
                session.sendMsg(ContentType.HTML, "Exception", requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR);
            }
        });
    }

    private ChangyanConfig changyanConfig(CommentWebsiteConfig websiteConfig) {
        if (websiteConfig == null || websiteConfig.getChangyan() == null || websiteConfig.getChangyan().trim().isEmpty()) {
            return new ChangyanConfig();
        }
        ChangyanConfig changyanConfig = gson.fromJson(websiteConfig.getChangyan(), ChangyanConfig.class);
        return changyanConfig == null ? new ChangyanConfig() : changyanConfig;
    }

    private void dealSyncRequest(final ChangyanComment changyanComment) {
        if (Objects.isNull(changyanComment)) {
            CommentService.recordSyncHistory(session, false, 0, "反向同步失败：数据包为空");
            return;
        }
        LOGGER.info("sync action " + changyanComment);
        int count = 0;
        try {
            if (changyanComment.getComments() != null) {
                for (CommentsEntry commentsEntry : changyanComment.getComments()) {
                    final Comment comment = getComment(changyanComment, commentsEntry);
                    CommentDAO.save(session, comment);
                    count++;
                }
            }
            CommentService.recordSyncHistory(session, true, count, "反向同步畅言评论 " + count + " 条成功");
        } catch (Exception e) {
            CommentService.recordSyncHistory(session, false, count, "反向同步失败: " + e.getMessage());
            throw e;
        }
    }

    private static Comment getComment(ChangyanComment changyanComment, CommentsEntry commentsEntry) {
        final Comment comment = new Comment();
        comment.setName(commentsEntry.getUser().getNickname());
        comment.setHeadPortrait(commentsEntry.getUser().getUsericon());
        comment.setLogId(changyanComment.getSourceid());
        comment.setIp(commentsEntry.getIp());
        comment.setContent(commentsEntry.getContent());
        comment.setCreatedTime(new Date(commentsEntry.getCtime()));
        comment.setPostId(commentsEntry.getCmtid());
        return comment;
    }
}
