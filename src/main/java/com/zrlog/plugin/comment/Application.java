package com.zrlog.plugin.comment;


import com.zrlog.plugin.comment.controller.CommentController;
import com.zrlog.plugin.client.NioClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Application {
    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        List<Class<?>> classList = new ArrayList<>();
        classList.add(CommentController.class);
        new NioClient(new CommentClientActionHandler()).connectServer(args, classList, CommentPluginAction.class);
    }
}

