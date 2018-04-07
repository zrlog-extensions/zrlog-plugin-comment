package com.zrlog.plugin.changyan.controller;

import com.zrlog.plugin.IMsgPacketCallBack;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.changyan.response.ChangyanComment;
import com.zrlog.plugin.changyan.response.CommentsEntry;
import com.zrlog.plugin.client.ClientActionHandler;
import com.zrlog.plugin.common.IdUtil;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.common.modle.Comment;
import com.zrlog.plugin.common.modle.PublicInfo;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.data.codec.MsgPacket;
import com.zrlog.plugin.data.codec.MsgPacketStatus;
import com.zrlog.plugin.render.SimpleTemplateRender;
import com.zrlog.plugin.type.ActionType;
import com.google.gson.Gson;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChangyanController {

    private static Logger LOGGER = LoggerUtil.getLogger(ChangyanController.class);

    private IOSession session;
    private MsgPacket requestPacket;
    private HttpRequestInfo requestInfo;

    public ChangyanController(IOSession session, MsgPacket requestPacket, HttpRequestInfo requestInfo) {
        this.session = session;
        this.requestPacket = requestPacket;
        this.requestInfo = requestInfo;
    }

    public void update() {
        session.sendMsg(new MsgPacket(requestInfo.simpleParam(), ContentType.JSON, MsgPacketStatus.SEND_REQUEST, IdUtil.getInt(), ActionType.SET_WEBSITE.name()), new IMsgPacketCallBack() {
            @Override
            public void handler(MsgPacket msgPacket) {
                Map<String, Object> map = new HashMap<>();
                map.put("success", true);
                session.sendMsg(new MsgPacket(map, ContentType.JSON, MsgPacketStatus.RESPONSE_SUCCESS, requestPacket.getMsgId(), requestPacket.getMethodStr()));
            }
        });
    }

    public void info() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", "appId,appKey,status,commentEmailNotify,callbackUrl");
        session.sendJsonMsg(keyMap, ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, new IMsgPacketCallBack() {
            @Override
            public void handler(MsgPacket msgPacket) {
                Map map = new Gson().fromJson(msgPacket.getDataStr(), Map.class);
                map.put("userName", requestInfo.getUserName());
                map.put("userId", requestInfo.getUserId());
                map.put("fullUrl", requestInfo.getFullUrl().replace("install", ""));
                if (map.get("callbackUrl") == null || "".equals(map.get("callbackUrl"))) {
                    map.put("callbackUrl", requestInfo.getAccessUrl() + "/p/" + session.getPlugin().getShortName() + "/sync/" + UUID.randomUUID().toString().replace("-", ""));
                }
                session.sendJsonMsg(map, requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
            }
        });
    }

    public void index() {
        session.responseHtml("/templates/index.html", new HashMap(), requestPacket.getMethodStr(), requestPacket.getMsgId());
    }

    public void widget() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", "appId");
        session.sendJsonMsg(keyMap, ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, new IMsgPacketCallBack() {
            @Override
            public void handler(MsgPacket msgPacket) {
                Map map = new Gson().fromJson(msgPacket.getDataStr(), Map.class);
                map.put("articleId", requestInfo.simpleParam().get("articleId"));
                session.responseHtmlStr(new SimpleTemplateRender().render("/templates/widget.html", session.getPlugin(), map), requestPacket.getMethodStr(), requestPacket.getMsgId());
            }
        });

    }

    /**
     * 反向同步接口
     */
    public void sync() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", "short_name,secret,status,commentEmailNotify,callbackUrl");
        session.sendJsonMsg(keyMap, ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, new IMsgPacketCallBack() {
            @Override
            public void handler(MsgPacket msgPacket) {
                Map<String, Object> changyan = new Gson().fromJson(msgPacket.getDataStr(), Map.class);
                String callbackUrl = (String) changyan.get("callbackUrl");
                String ignoreChar = "/p" + "/" + session.getPlugin().getShortName();
                try {
                    if (callbackUrl != null && new URL(callbackUrl).getPath().replace(ignoreChar, "").equals(requestInfo.getUri().replace(".action", ""))) {
                        String commentJsonStr = requestInfo.getParam().get("data")[0];
                        LOGGER.info(commentJsonStr);
                        final ChangyanComment changyanComment = new Gson().fromJson(commentJsonStr, ChangyanComment.class);
                        Map<String, Object> response = new HashMap<>();
                        dealSyncRequest(response, changyanComment, "on".equals(changyan.get("commentEmailNotify")));
                    } else {
                        session.sendMsg(ContentType.HTML, ClientActionHandler.ACTION_NOT_FOUND_PAGE, requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "", e);
                    session.sendMsg(ContentType.HTML, "Exception", requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR);
                }
            }
        });

    }

    private void dealSyncRequest(final Map<String, Object> response, final ChangyanComment changyanComment, final boolean emailNotify) {
        if (changyanComment != null) {
            LOGGER.info("sync action " + changyanComment);
            for (CommentsEntry commentsEntry : changyanComment.getComments()) {
                final Comment comment = new Comment();
                comment.setName(commentsEntry.getUser().getNickname());
                comment.setHeadPortrait(commentsEntry.getUser().getUsericon());
                comment.setLogId(Long.valueOf(changyanComment.getSourceid()));
                comment.setIp(commentsEntry.getIp());
                comment.setContent(commentsEntry.getContent());
                comment.setCreatedTime(new Date(commentsEntry.getCtime()));
                comment.setPostId(commentsEntry.getCmtid().longValue());

                LOGGER.log(Level.INFO, "changyan call " + new Gson().toJson(comment));
                session.sendMsg(ContentType.JSON, comment, ActionType.ADD_COMMENT.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, new IMsgPacketCallBack() {
                    @Override
                    public void handler(MsgPacket msgPacket) {
                        response.put("status", msgPacket.getStatus() == MsgPacketStatus.RESPONSE_SUCCESS ? 200 : 500);
                        session.sendMsg(ContentType.JSON, response, requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
                    }
                });
                if (emailNotify) {
                    session.sendMsg(ContentType.JSON, new HashMap<>(), ActionType.LOAD_PUBLIC_INFO.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, new IMsgPacketCallBack() {
                        @Override
                        public void handler(MsgPacket msgPacket) {
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
                        }
                    });
                }
            }
        }
    }
}