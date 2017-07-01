package com.fzb.zrlog.plugin.changyan;


import com.fzb.zrlog.plugin.changyan.controller.ChangyanController;
import com.fzb.zrlog.plugin.client.NioClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Start {
    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        List<Class> classList = new ArrayList<>();
        classList.add(ChangyanController.class);
        new NioClient(new ChangyanClientActionHandler()).connectServerByProperties(args, classList, "/plugin.properties", ChangyanPluginAction.class);
    }
}

