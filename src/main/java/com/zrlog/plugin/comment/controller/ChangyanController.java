package com.zrlog.plugin.comment.controller;

import com.google.gson.Gson;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.client.ClientActionHandler;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
        keyMap.put("key", "changyan");
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

    private void dealSyncRequest(final ChangyanComment changyanComment) {
        if (Objects.isNull(changyanComment)) {
            return;
        }
        LOGGER.info("sync action " + changyanComment);
        for (CommentsEntry commentsEntry : changyanComment.getComments()) {
            final Comment comment = getComment(changyanComment, commentsEntry);
            CommentDAO.save(session, comment);
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
