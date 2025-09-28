package com.zrlog.plugin.comment;

import com.google.gson.Gson;
import com.zrlog.plugin.comment.controller.CommentController;
import com.zrlog.plugin.comment.response.ChangyanComment;
import com.zrlog.plugin.comment.response.CommentsEntry;
import com.zrlog.plugin.comment.response.User;
import com.zrlog.plugin.common.PluginNativeImageUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class GraalvmAgentApplication {


    public static void main(String[] args) throws IOException {
        PluginNativeImageUtils.usedGsonObject();
        new Gson().toJson(new ChangyanComment());
        new Gson().toJson(new CommentsEntry());
        new Gson().toJson(new User());
        String basePath = System.getProperty("user.dir").replace("\\target", "").replace("/target", "");
        //PathKit.setRootPath(basePath);
        File file = new File(basePath + "/src/main/resources");
        PluginNativeImageUtils.doLoopResourceLoad(file.listFiles(), file.getPath() + "/", "/");
        //Application.nativeAgent = true;
        PluginNativeImageUtils.exposeController(Collections.singletonList(CommentController.class));
        Application.main(args);

    }
}