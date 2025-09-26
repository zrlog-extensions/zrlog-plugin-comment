package com.zrlog.plugin.comment.controller;

import com.google.gson.Gson;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.client.ClientActionHandler;
import com.zrlog.plugin.comment.response.ChangyanComment;
import com.zrlog.plugin.comment.response.CommentsEntry;
import com.zrlog.plugin.common.IdUtil;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.common.model.Comment;
import com.zrlog.plugin.common.model.PublicInfo;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.data.codec.MsgPacket;
import com.zrlog.plugin.data.codec.MsgPacketStatus;
import com.zrlog.plugin.render.SimpleTemplateRender;
import com.zrlog.plugin.type.ActionType;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChangyanController {

    private static final Logger LOGGER = LoggerUtil.getLogger(ChangyanController.class);

    private final IOSession session;
    private final MsgPacket requestPacket;
    private final HttpRequestInfo requestInfo;

    public ChangyanController(IOSession session, MsgPacket requestPacket, HttpRequestInfo requestInfo) {
        this.session = session;
        this.requestPacket = requestPacket;
        this.requestInfo = requestInfo;
    }

    /**
     * 反向同步接口
     */
    public void sync() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", "changyan,commentEmailNotify");
        session.sendJsonMsg(keyMap, ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, msgPacket -> {
            Map<String, Object> data = new Gson().fromJson(msgPacket.getDataStr(), Map.class);
            Map<String, Object> changyan = new Gson().fromJson((String) data.get("changyan"), Map.class);
            String callbackUrl = (String) changyan.get("callbackUrl");
            String ignoreChar = "/p/" + session.getPlugin().getShortName();
            try {
                if (callbackUrl != null && new URL(callbackUrl).getPath().replace(ignoreChar, "").equals(requestInfo.getUri().replace(".action", ""))) {
                    String commentJsonStr = requestInfo.getParam().get("data")[0];
                    LOGGER.info(commentJsonStr);
                    final ChangyanComment changyanComment = new Gson().fromJson(commentJsonStr, ChangyanComment.class);
                    Map<String, Object> response = new HashMap<>();
                    dealSyncRequest(response, changyanComment, "on".equals(data.get("commentEmailNotify")));
                } else {
                    session.sendMsg(ContentType.HTML, ClientActionHandler.ACTION_NOT_FOUND_PAGE, requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "", e);
                session.sendMsg(ContentType.HTML, "Exception", requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR);
            }
        });
    }

    private void dealSyncRequest(final Map<String, Object> response, final ChangyanComment changyanComment, final boolean emailNotify) {
        if (changyanComment != null) {
            LOGGER.info("sync action " + changyanComment);
            for (CommentsEntry commentsEntry : changyanComment.getComments()) {
                final Comment comment = getComment(changyanComment, commentsEntry);

                LOGGER.log(Level.INFO, "changyan call " + new Gson().toJson(comment));
                session.sendMsg(ContentType.JSON, comment, ActionType.ADD_COMMENT.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, msgPacket -> {
                    response.put("status", msgPacket.getStatus() == MsgPacketStatus.RESPONSE_SUCCESS ? 200 : 500);
                    session.sendMsg(ContentType.JSON, response, requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
                });
                if (emailNotify) {
                    session.sendMsg(ContentType.JSON, new HashMap<>(), ActionType.LOAD_PUBLIC_INFO.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, msgPacket -> {
                        PublicInfo publicInfo = msgPacket.convertToClass(PublicInfo.class);
                        Map<String, String> map = new HashMap<>();
                        Map<String, Object> moduleMap = new HashMap<>();
                        moduleMap.put("content", comment.getContent());
                        moduleMap.put("title", changyanComment.getTitle());
                        moduleMap.put("titleUrl", changyanComment.getUrl());
                        moduleMap.put("username", comment.getName());
                        moduleMap.put("version", session.getPlugin().getVersion());
                        map.put("content", new SimpleTemplateRender().render("/email/notify-email.html", session.getPlugin(), moduleMap));
                        map.put("title", publicInfo.getTitle() + " 有了新的评论");
                        session.requestService("emailService", map);
                    });
                }
            }
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
