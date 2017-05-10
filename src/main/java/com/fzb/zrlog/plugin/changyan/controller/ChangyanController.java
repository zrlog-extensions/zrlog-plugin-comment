package com.fzb.zrlog.plugin.changyan.controller;

import com.fzb.zrlog.plugin.IMsgPacketCallBack;
import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.changyan.response.Meta;
import com.fzb.zrlog.plugin.changyan.response.ResponseEntry;
import com.fzb.zrlog.plugin.changyan.util.ChangyanUtil;
import com.fzb.zrlog.plugin.common.IdUtil;
import com.fzb.zrlog.plugin.common.modle.Comment;
import com.fzb.zrlog.plugin.common.modle.PublicInfo;
import com.fzb.zrlog.plugin.data.codec.ContentType;
import com.fzb.zrlog.plugin.data.codec.HttpRequestInfo;
import com.fzb.zrlog.plugin.data.codec.MsgPacket;
import com.fzb.zrlog.plugin.data.codec.MsgPacketStatus;
import com.fzb.zrlog.plugin.type.ActionType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import flexjson.JSONDeserializer;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangyanController {

    private static Logger LOGGER = Logger.getLogger(ChangyanController.class);

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
        keyMap.put("key", "appId,appKey,status,commentEmailNotify");
        session.sendJsonMsg(keyMap, ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, new IMsgPacketCallBack() {
            @Override
            public void handler(MsgPacket msgPacket) {
                Map map = new JSONDeserializer<Map>().deserialize(msgPacket.getDataStr());
                map.put("userName", requestInfo.getUserName());
                map.put("userId", requestInfo.getUserId());
                map.put("fullUrl", requestInfo.getFullUrl().replace("install", ""));
                session.sendJsonMsg(map, requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
            }
        });
    }

    public void index() {
        session.responseHtml("/templates/index.html", new HashMap(), requestPacket.getMethodStr(), requestPacket.getMsgId());
    }

    /**
     * 反向同步接口
     */
    public void sync() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", "short_name,secret,status,commentEmailNotify");
        session.sendJsonMsg(keyMap, ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, new IMsgPacketCallBack() {
            @Override
            public void handler(MsgPacket msgPacket) {
                Map<String, Object> param = getRequest();
                String action = (String) param.get("action");
                String signature = (String) param.get("signature");
                param.remove("signature");
                Map<String, Object> duoshuo = new JSONDeserializer<Map<String, Object>>().deserialize(msgPacket.getDataStr());
                LOGGER.info("param = " + param);

                final Map<String, Object> response = new HashMap<>();
                try {
                    // check signature
                    response.put("status", 400);
                    if (param.isEmpty() || !signature.equals(ChangyanUtil.hmacSHA1Encrypt(param, duoshuo.get("secret") + ""))) {
                        response.put("status", 400);
                        session.sendMsg(ContentType.JSON, response, requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR);
                    } else {
                        dealSyncRequest(response, ChangyanUtil.getCommentLast(duoshuo.get("short_name") + "", duoshuo.get("secret") + ""), duoshuo);
                    }
                } catch (Exception e) {
                    response.put("status", 500);
                    LOGGER.error("doushuo sync error ", e);
                    session.sendMsg(ContentType.JSON, response, requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR);
                }
                LOGGER.info("action " + action);
            }
        });

    }

    private void dealSyncRequest(final Map<String, Object> response, final ResponseEntry entry, final Map<String, Object> duoshuo) {
        if (entry != null) {
            LOGGER.info("sync action " + entry.getAction());
            if (entry.getAction().equals("create")) {
                Meta meta = new GsonBuilder().create().fromJson(new Gson().toJson(entry.getMeta()),
                        new TypeToken<Meta>() {
                        }.getType());
                final Comment comment = ChangyanUtil.convertToSelf(meta);
                session.sendMsg(ContentType.JSON, comment, ActionType.ADD_COMMENT.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, new IMsgPacketCallBack() {
                    @Override
                    public void handler(MsgPacket msgPacket) {
                        response.put("status", msgPacket.getStatus() == MsgPacketStatus.RESPONSE_SUCCESS ? 200 : 500);
                        session.sendMsg(ContentType.JSON, response, requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
                    }
                });
                if ("on".equals(duoshuo.get("commentEmailNotify"))) {
                    session.sendMsg(ContentType.JSON, new HashMap<>(), ActionType.LOAD_PUBLIC_INFO.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, new IMsgPacketCallBack() {
                        @Override
                        public void handler(MsgPacket msgPacket) {
                            PublicInfo publicInfo = msgPacket.convertToClass(PublicInfo.class);
                            Map<String, String> map = new HashMap<>();
                            map.put("content", comment.getName() + " 评论了 " + comment.getContent() + " <a href='" + publicInfo.getHomeUrl() + "post/" + comment.getLogId() + "'>点击查看</a>");
                            map.put("title", publicInfo.getTitle() + "有了新的评论");
                            session.requestService("emailService", map);
                        }
                    });
                }
            } else if (entry.getAction().equals("delete")) {
                List<String> l = (List<String>) entry.getMeta();
                for (String postId : l) {
                    //TODO just delete one record
                    Comment comment = new Comment();
                    comment.setPostId(Long.valueOf(postId));
                    session.sendMsg(ContentType.JSON, comment, ActionType.DELETE_COMMENT.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, new IMsgPacketCallBack() {
                        @Override
                        public void handler(MsgPacket msgPacket) {
                            response.put("status", msgPacket.getStatus() == MsgPacketStatus.RESPONSE_SUCCESS ? 200 : 500);
                            session.sendMsg(ContentType.JSON, response, requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
                        }
                    });
                    break;
                }
            } else {
                LOGGER.warn("UnSupport action " + entry.getAction());
            }
        } else {
            LOGGER.error("why ?????????????????");
        }
    }

    private Map<String, Object> getRequest() {
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setHeaderEncoding("UTF-8");

        Map<String, Object> map = new HashMap<String, Object>();
        try {
            ChangyanRequestContext context = new ChangyanRequestContext();
            context.setCharacterEncoding("UTF-8");
            if (requestInfo.getHeader().get("Content-Length") == null) {
                return map;
            }
            context.setContentLength(Integer.parseInt(requestInfo.getHeader().get("Content-Length").toString()));
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(requestInfo.getRequestBody());
            context.setInputStream(byteArrayInputStream);
            context.setContentType(requestInfo.getHeader().get("Content-Type").toString());
            List items = upload.parseRequest(context);
            for (Object item1 : items) {
                FileItem item = (FileItem) item1;
                if (item.isFormField()) {
                    map.put(item.getFieldName(), item.getString());
                }
            }
        } catch (FileUploadException e) {
            LOGGER.error("parse duoshou param error ", e);
        }
        return map;
    }

    public void refresh() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", "appId,appKey,status");
        session.sendJsonMsg(keyMap, ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, new IMsgPacketCallBack() {
            @Override
            public void handler(MsgPacket msgPacket) {
                Map<String, Object> map = new JSONDeserializer<Map<String, Object>>().deserialize(msgPacket.getDataStr());
                List<ResponseEntry> responseEntryList = ChangyanUtil.getComments(map.get("appId") + "", map.get("appKey") + "");
                for (ResponseEntry entry : responseEntryList) {
                    dealSyncRequest(new HashMap<String, Object>(), entry, map);
                }
            }
        });
    }
}