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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommentController {

    private static final Logger LOGGER = LoggerUtil.getLogger(CommentController.class);

    private final IOSession session;
    private final MsgPacket requestPacket;
    private final HttpRequestInfo requestInfo;

    public CommentController(IOSession session, MsgPacket requestPacket, HttpRequestInfo requestInfo) {
        this.session = session;
        this.requestPacket = requestPacket;
        this.requestInfo = requestInfo;
    }

    public void update() {
        session.sendMsg(new MsgPacket(requestInfo.simpleParam(), ContentType.JSON, MsgPacketStatus.SEND_REQUEST, IdUtil.getInt(),
                ActionType.SET_WEBSITE.name()), msgPacket -> {
            Map<String, Object> map = new HashMap<>();
            map.put("success", true);
            session.sendMsg(new MsgPacket(map, ContentType.JSON, MsgPacketStatus.RESPONSE_SUCCESS, requestPacket.getMsgId(), requestPacket.getMethodStr()));
        });
    }

    public void index() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", "changyang,base,commentEmailNotify,type");
        session.sendJsonMsg(keyMap, ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, msgPacket -> {
            Map map = new Gson().fromJson(msgPacket.getDataStr(), Map.class);
            map.put("userName", requestInfo.getUserName());
            map.put("userId", requestInfo.getUserId());
            map.put("fullUrl", requestInfo.getFullUrl().replace("install", ""));
            if (map.get("callbackUrl") == null || "".equals(map.get("callbackUrl"))) {
                map.put("callbackUrl", requestInfo.getAccessUrl() + "/p/" + session.getPlugin().getShortName() + "/changyan/sync/" + UUID.randomUUID().toString().replace("-", ""));
            }
            if (!Objects.equals(map.get("commentEmailNotify"), "on")) {
                map.remove("commentEmailNotify");
            }
            Map<String, Object> data = new HashMap<>();
            data.put("theme", Objects.equals(requestInfo.getHeader().get("Dark-Mode"), "true") ? "dark" : "light");
            data.put("data", new Gson().toJson(map));
            session.responseHtmlStr(new SimpleTemplateRender().render("/templates/index", session.getPlugin(), data), requestPacket.getMethodStr(), requestPacket.getMsgId());
        });

    }

    public void widget() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", "appId,type");
        session.sendJsonMsg(keyMap, ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, msgPacket -> {
            Map map = new Gson().fromJson(msgPacket.getDataStr(), Map.class);
            String articleId = (String) requestInfo.simpleParam().get("articleId");
            if (Objects.isNull(articleId)) {
                articleId = "-1";
            }
            map.put("articleId", articleId);
            session.responseHtmlStr(new SimpleTemplateRender().render("/templates/widget/" + keyMap.get("type") + "/index.html", session.getPlugin(), map), requestPacket.getMethodStr(), requestPacket.getMsgId());
        });

    }

}