package com.zrlog.plugin.comment.controller;

import com.google.gson.Gson;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.comment.config.ChangyanConfig;
import com.zrlog.plugin.comment.config.CommentApiResponse;
import com.zrlog.plugin.comment.config.CommentBaseConfig;
import com.zrlog.plugin.comment.config.CommentHistoryConfig;
import com.zrlog.plugin.comment.config.CommentHistoryRecord;
import com.zrlog.plugin.comment.config.CommentSubmitRequest;
import com.zrlog.plugin.comment.config.CommentUpdateRequest;
import com.zrlog.plugin.comment.config.CommentWebsiteConfig;
import com.zrlog.plugin.comment.config.WebsiteKeyRequest;
import com.zrlog.plugin.comment.dao.CommentDAO;
import com.zrlog.plugin.comment.service.CommentService;
import com.zrlog.plugin.common.IdUtil;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.common.model.Comment;
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
    private final Gson gson = new Gson();

    public CommentController(IOSession session, MsgPacket requestPacket, HttpRequestInfo requestInfo) {
        this.session = session;
        this.requestPacket = requestPacket;
        this.requestInfo = requestInfo;
    }

    public void update() {
        session.sendMsg(new MsgPacket(updateRequest(), ContentType.JSON, MsgPacketStatus.SEND_REQUEST, IdUtil.getInt(),
                ActionType.SET_WEBSITE.name()), msgPacket -> {
            CommentService.recordSyncHistory(session, true, 0, "更新插件配置参数成功");
            response(CommentApiResponse.success());
        });
    }

    public void json() {
        session.sendJsonMsg(data(), requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
    }

    public void history() {
        CommentHistoryConfig historyConfig = session.getResponseSync(ContentType.JSON, WebsiteKeyRequest.of("syncHistory"),
                ActionType.GET_WEBSITE, CommentHistoryConfig.class);
        session.sendJsonMsg(historyList(historyConfig), requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
    }

    private void doComment() {
        CommentSubmitRequest submitRequest = submitRequest();
        if (Objects.isNull(submitRequest.getLogId()) || submitRequest.getLogId().trim().isEmpty()) {
            throw new RuntimeException("文章id 不能为空");
        }
        String content = submitRequest.getUserComment();
        if (Objects.isNull(content) || content.trim().isEmpty()) {
            throw new RuntimeException("内容不能为空");
        }
        String userName = submitRequest.getUserName();
        if (Objects.isNull(userName) || userName.trim().isEmpty()) {
            throw new RuntimeException("昵称不能为空");
        }

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setName(userName);
        comment.setLogId(Long.parseLong(submitRequest.getLogId()));
        comment.setHome(submitRequest.getWeb());
        comment.setMail(submitRequest.getEmail());
        comment.setHeadPortrait("");
        comment.setIp(requestInfo.getHeader().get("X-Real-IP"));
        comment.setCreatedTime(new Date());
        CommentDAO.save(session, comment);
    }

    public void addComment() {
        Map<String, Object> map = new HashMap<>();
        try {
            doComment();
            map.put("resultMsg", "评论成功");
        } catch (Exception e) {
            map.put("resultMsg", e.getMessage());
        } finally {
            session.responseHtmlStr(new SimpleTemplateRender().render("/result/comment", session.getPlugin(), map), requestPacket.getMethodStr(), requestPacket.getMsgId());
        }
    }

    private Map<String, Object> data() {
        CommentWebsiteConfig config = websiteConfig("changyan,base,commentEmailNotify,type,syncHistory");
        config.setUserName(requestInfo.getUserName());
        config.setUserId(requestInfo.getUserId());
        config.setFullUrl(requestInfo.getFullUrl().replace("install", ""));
        config.normalize(requestInfo.getAccessUrl() + "/p/" + session.getPlugin().getShortName() + "/changyan/sync/"
                + UUID.randomUUID().toString().replace("-", ""));
        Map<String, Object> data = new HashMap<>();
        data.put("theme", requestInfo.isDarkMode() ? "dark" : "light");
        data.put("dark", requestInfo.isDarkMode());
        data.put("setting", config);
        data.put("primaryColor", requestInfo.getAdminColorPrimary());
        data.put("colorPrimary", requestInfo.getAdminColorPrimary());
        data.put("plugin", session.getPlugin());
        return data;
    }

    public void index() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("data", gson.toJson(data()));
        session.responseHtmlStr(new SimpleTemplateRender().render("/templates/index", session.getPlugin(), keyMap), requestPacket.getMethodStr(), requestPacket.getMsgId());
    }

    public void widget() {
        CommentWebsiteConfig config = websiteConfig("base,changyan,type");
        String articleId = paramValue("articleId");
        if (Objects.isNull(articleId)) {
            articleId = "-1";
        }
        Map<String, Object> data = new HashMap<>();
        data.put("articleId", articleId);
        String type = config.normalizedType();
        if (Objects.isNull(type) || type.trim().isEmpty()) {
            type = "base";
            data.put("type", "base");
        }
        if (Objects.equals(type, "base")) {
            fillBaseCommentInfo(config, data);
            data.put("comments", new CommentService().renderBaseListCommentHtml(session, Long.parseLong(articleId)));
        } else if (Objects.equals(type, "changyan")) {
            ChangyanConfig changyanConfig = changyanConfig(config.getChangyan());
            String appId = changyanConfig.getAppId();
            if (Objects.nonNull(appId)) {
                data.put("appId", appId);
            } else {
                data.put("appId", "");
            }
        }
        session.responseHtmlStr(new SimpleTemplateRender().render("/widget/" + type + "/index", session.getPlugin(), data), requestPacket.getMethodStr(), requestPacket.getMsgId());
    }

    private void fillBaseCommentInfo(CommentWebsiteConfig config, Map<String, Object> data) {
        CommentBaseConfig baseConfig = baseConfig(config.getBase());
        data.put("styleStr", blankToDefault(baseConfig.getStyleStr(), ""));
        data.put("mainColor", blankToDefault(baseConfig.getMainColor(), "#1677ff"));
        String baseUrl = baseConfig.getBaseUrl();
        if (Objects.isNull(baseUrl) || Objects.equals(baseUrl, "")) {
            data.put("commentUrl", "/p/comment/addComment");
        } else {
            data.put("commentUrl", baseUrl + "/p/comment/addComment");
        }
    }

    private List<CommentHistoryRecord> historyList(CommentHistoryConfig historyConfig) {
        String historyJson = historyConfig == null ? null : historyConfig.getSyncHistory();
        if (historyJson == null || historyJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        CommentHistoryRecord[] records = gson.fromJson(historyJson, CommentHistoryRecord[].class);
        if (records == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(records));
    }

    private CommentWebsiteConfig websiteConfig(String keys) {
        CommentWebsiteConfig config = session.getResponseSync(ContentType.JSON, WebsiteKeyRequest.of(keys), ActionType.GET_WEBSITE,
                CommentWebsiteConfig.class);
        return config == null ? new CommentWebsiteConfig() : config;
    }

    private CommentUpdateRequest updateRequest() {
        CommentUpdateRequest request = new CommentUpdateRequest();
        request.setType(paramValue("type"));
        request.setCommentEmailNotify(paramValue("commentEmailNotify"));
        request.setChangyan(paramValue("changyan"));
        request.setBase(paramValue("base"));
        return request;
    }

    private CommentSubmitRequest submitRequest() {
        CommentSubmitRequest request = new CommentSubmitRequest();
        request.setLogId(paramValue("logId"));
        request.setUserComment(paramValue("userComment"));
        request.setUserName(paramValue("userName"));
        request.setWeb(paramValue("web"));
        request.setEmail(paramValue("email"));
        return request;
    }

    private String paramValue(String key) {
        if (requestInfo.getParam() == null || requestInfo.getParam().get(key) == null || requestInfo.getParam().get(key).length == 0) {
            return null;
        }
        return requestInfo.getParam().get(key)[0];
    }

    private CommentBaseConfig baseConfig(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new CommentBaseConfig();
        }
        CommentBaseConfig config = gson.fromJson(value, CommentBaseConfig.class);
        return config == null ? new CommentBaseConfig() : config;
    }

    private ChangyanConfig changyanConfig(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ChangyanConfig();
        }
        ChangyanConfig config = gson.fromJson(value, ChangyanConfig.class);
        return config == null ? new ChangyanConfig() : config;
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    private void response(Object data) {
        session.sendMsg(new MsgPacket(data, ContentType.JSON, MsgPacketStatus.RESPONSE_SUCCESS, requestPacket.getMsgId(),
                requestPacket.getMethodStr()));
    }
}
