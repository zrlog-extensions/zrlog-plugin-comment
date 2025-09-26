package com.zrlog.plugin.comment;

import com.google.gson.Gson;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.client.ClientActionHandler;
import com.zrlog.plugin.comment.controller.ChangyanController;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.data.codec.MsgPacket;

public class CommentClientActionHandler extends ClientActionHandler {

    @Override
    public void httpMethod(IOSession session, MsgPacket msgPacket) {
        HttpRequestInfo httpRequestInfo = new Gson().fromJson(msgPacket.getDataStr(), HttpRequestInfo.class);
        if (httpRequestInfo.getUri().startsWith("/changyan/sync/")) {
            new ChangyanController(session, msgPacket, httpRequestInfo).sync();
        } else {
            super.httpMethod(session, msgPacket);
        }
    }
}
