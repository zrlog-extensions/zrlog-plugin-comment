package com.zrlog.plugin.comment;

import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.comment.controller.CommentController;
import com.zrlog.plugin.client.ClientActionHandler;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.data.codec.MsgPacket;
import com.google.gson.Gson;

public class CommentClientActionHandler extends ClientActionHandler {

    @Override
    public void httpMethod(IOSession session, MsgPacket msgPacket) {
        HttpRequestInfo httpRequestInfo = new Gson().fromJson(msgPacket.getDataStr(),HttpRequestInfo.class);
        if (httpRequestInfo.getUri().startsWith("/sync/")) {
            new CommentController(session, msgPacket, httpRequestInfo).sync();
        } else {
            super.httpMethod(session, msgPacket);
        }
    }
}
