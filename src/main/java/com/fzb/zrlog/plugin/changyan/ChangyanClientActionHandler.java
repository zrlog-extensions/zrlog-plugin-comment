package com.fzb.zrlog.plugin.changyan;

import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.changyan.controller.ChangyanController;
import com.fzb.zrlog.plugin.client.ClientActionHandler;
import com.fzb.zrlog.plugin.data.codec.HttpRequestInfo;
import com.fzb.zrlog.plugin.data.codec.MsgPacket;
import com.google.gson.Gson;

public class ChangyanClientActionHandler extends ClientActionHandler {

    @Override
    public void httpMethod(IOSession session, MsgPacket msgPacket) {
        HttpRequestInfo httpRequestInfo = new Gson().fromJson(msgPacket.getDataStr(),HttpRequestInfo.class);
        if (httpRequestInfo.getUri().startsWith("/sync/")) {
            new ChangyanController(session, msgPacket, httpRequestInfo).sync();
        } else {
            super.httpMethod(session, msgPacket);
        }
    }
}
