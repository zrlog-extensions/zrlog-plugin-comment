package com.zrlog.plugin.comment.dao;

import com.google.gson.Gson;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.client.HttpClientUtils;
import com.zrlog.plugin.common.IdUtil;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.common.model.Comment;
import com.zrlog.plugin.common.model.PublicInfo;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.data.codec.MsgPacketStatus;
import com.zrlog.plugin.render.SimpleTemplateRender;
import com.zrlog.plugin.type.ActionType;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommentDAO {

    private static final Logger LOGGER = LoggerUtil.getLogger(CommentDAO.class);

    public static void save(IOSession session, Comment comment) {
        LOGGER.log(Level.INFO, "new comment " + new Gson().toJson(comment));
        session.sendMsg(ContentType.JSON, comment, ActionType.ADD_COMMENT.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, addMsgPacket -> {
            Map<String, Object> response = new HashMap<>();
            response.put("status", addMsgPacket.getStatus() == MsgPacketStatus.RESPONSE_SUCCESS ? 200 : 500);
            session.sendMsg(ContentType.JSON, response, addMsgPacket.getMethodStr(), addMsgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
            tryNotify(session, comment);
        });
    }

    private static void tryNotify(IOSession session, Comment comment) {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", "commentEmailNotify");
        if (!Objects.equals(keyMap.get("commentEmailNotify"), "true")) {
            return;
        }
        PublicInfo publicInfo = session.getResponseSync(ContentType.JSON, new HashMap<>(), ActionType.LOAD_PUBLIC_INFO, PublicInfo.class);
        Map<String, String> map = new HashMap<>();
        Map<String, Object> moduleMap = new HashMap<>();
        moduleMap.put("content", comment.getContent());
        moduleMap.put("title", "-");
        moduleMap.put("titleUrl", "-");
        moduleMap.put("username", comment.getName());
        moduleMap.put("version", session.getPlugin().getVersion());
        map.put("content", new SimpleTemplateRender().render("/email/notify-email.html", session.getPlugin(), moduleMap));
        map.put("title", publicInfo.getTitle() + " 有了新的评论");
        session.requestService("emailService", map);
    }

    public static List<Map<String, Object>> loadComments(IOSession session, Long articleId) {
        PublicInfo publicInfo = session.getResponseSync(ContentType.JSON, new HashMap<>(), ActionType.LOAD_PUBLIC_INFO, PublicInfo.class);
        Map map = HttpClientUtils.sendGetRequest(publicInfo.getApiHomeUrl() + "/api/article/comment?id=" + articleId, Map.class, new HashMap<>(), session, Duration.ofSeconds(30));
        return (List<Map<String, Object>>) map.get("data");
    }
}
