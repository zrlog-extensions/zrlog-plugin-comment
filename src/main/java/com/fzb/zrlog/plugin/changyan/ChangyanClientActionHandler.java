package com.fzb.zrlog.plugin.changyan;

import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.changyan.controller.ChangyanController;
import com.fzb.zrlog.plugin.client.ClientActionHandler;
import com.fzb.zrlog.plugin.data.codec.HttpRequestInfo;
import com.fzb.zrlog.plugin.data.codec.MsgPacket;
import flexjson.JSONDeserializer;

public class ChangyanClientActionHandler extends ClientActionHandler {

    @Override
    public void httpMethod(IOSession session, MsgPacket msgPacket) {
        HttpRequestInfo httpRequestInfo = (HttpRequestInfo) (new JSONDeserializer()).deserialize(msgPacket.getDataStr());
        if (httpRequestInfo.getUri().startsWith("/sync/")) {
            new ChangyanController(session, msgPacket, httpRequestInfo).sync();
        } else {
            super.httpMethod(session, msgPacket);
        }
    }
}
