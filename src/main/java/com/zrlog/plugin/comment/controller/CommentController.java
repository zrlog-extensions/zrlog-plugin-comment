package com.zrlog.plugin.comment.controller;

import com.google.gson.Gson;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.comment.dao.CommentDAO;
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

import java.util.*;
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

    public void json() {
        session.sendJsonMsg(data(), requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
    }

    public void addComment() {
        Map map = requestInfo.simpleParam();
        Comment comment = new Comment();
        comment.setContent(map.get("userComment").toString());
        comment.setHome(map.get("web").toString());
        comment.setMail(map.get("email").toString());
        comment.setHeadPortrait("");
        comment.setName(map.get("userName").toString());
        comment.setIp(requestInfo.getHeader().get("X-Real-IP"));
        comment.setCreatedTime(new Date());
        comment.setLogId(Long.parseLong((String) map.get("logId")));
        CommentDAO.save(session, comment);
        session.responseHtmlStr("success", requestPacket.getMethodStr(), requestPacket.getMsgId());
    }

    private Map<String, Object> data() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", "changyan,base,commentEmailNotify,type");
        Map map = session.getResponseSync(ContentType.JSON, keyMap, ActionType.GET_WEBSITE, Map.class);
        map.put("userName", requestInfo.getUserName());
        map.put("userId", requestInfo.getUserId());
        map.put("fullUrl", requestInfo.getFullUrl().replace("install", ""));
        if (map.get("changyan") == null || "".equals(map.get("changyan"))) {
            Map<String, Object> defaultChangyan = new HashMap<>();
            defaultChangyan.put("callbackUrl", requestInfo.getAccessUrl() + "/p/" + session.getPlugin().getShortName() + "/changyan/sync/" + UUID.randomUUID().toString().replace("-", ""));
            map.put("changyan", new Gson().toJson(defaultChangyan));
        }
        if (map.get("base") == null || "".equals(map.get("base"))) {
            Map<String, Object> defaultBase = new HashMap<>();
            defaultBase.put("styleStr", "");
            map.put("base", new Gson().toJson(defaultBase));
        }
        if (Objects.isNull(map.get("type"))) {
            map.put("type", "base");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("darkMode", Objects.equals(requestInfo.getHeader().get("Dark-Mode"), "true"));
        data.put("setting", map);
        PublicInfo publicInfo = session.getResponseSync(ContentType.JSON, data, ActionType.LOAD_PUBLIC_INFO, PublicInfo.class);
        data.put("primaryColor", publicInfo.getAdminColorPrimary());
        data.put("plugin", session.getPlugin());
        return data;
    }

    public void index() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("data", new Gson().toJson(data()));
        session.responseHtmlStr(new SimpleTemplateRender().render("/templates/index", session.getPlugin(), keyMap), requestPacket.getMethodStr(), requestPacket.getMsgId());
    }

    public void widget() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", "base,changyan,type");
        Map configMap = session.getResponseSync(ContentType.JSON, keyMap, ActionType.GET_WEBSITE, Map.class);
        String articleId = (String) requestInfo.simpleParam().get("articleId");
        if (Objects.isNull(articleId)) {
            articleId = "-1";
        }
        Map<String, Object> data = new HashMap<>();
        data.put("articleId", articleId);
        String type = (String) configMap.get("type");
        if (Objects.isNull(type) || type.trim().isEmpty()) {
            type = "base";
            data.put("type", "base");
        }
        if (Objects.equals(type, "base")) {
            fillBaseCommentInfo(configMap, data);
        }
        session.responseHtmlStr(new SimpleTemplateRender().render("/templates/widget/" + configMap.get("type") + "/index", session.getPlugin(), data), requestPacket.getMethodStr(), requestPacket.getMsgId());
    }

    private static void fillBaseCommentInfo(Map configMap, Map<String, Object> data) {
        Map map = Objects.nonNull(configMap.get("base")) ? new Gson().fromJson((String) configMap.get("base"), Map.class) : new HashMap<>();
        data.put("styleStr", map.get("styleStr"));
        String baseUrl = (String) map.get("baseUrl");
        if (Objects.isNull(baseUrl) || Objects.equals(baseUrl, "")) {
            data.put("commentUrl", "/p/comment/addComment");
        } else {
            data.put("commentUrl", baseUrl + "/p/comment/addComment");
        }
        data.put("comments", "");
    }

}