package com.zrlog.plugin.changyan;


import com.zrlog.plugin.changyan.controller.ChangyanController;
import com.zrlog.plugin.client.NioClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Application {
    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        List<Class> classList = new ArrayList<>();
        classList.add(ChangyanController.class);
        new NioClient(new ChangyanClientActionHandler()).connectServer(args, classList, ChangyanPluginAction.class);
    }
}

