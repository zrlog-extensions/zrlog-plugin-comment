package com.zrlog.plugin.comment;

import com.google.gson.Gson;
import com.zrlog.plugin.comment.controller.CommentController;
import com.zrlog.plugin.comment.response.ChangyanComment;
import com.zrlog.plugin.comment.response.CommentsEntry;
import com.zrlog.plugin.comment.response.User;
import com.zrlog.plugin.common.PluginNativeImageUtils;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.message.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class GraalvmAgentApplication {


    public static void main(String[] args) throws IOException {
        new Gson().toJson(new HttpRequestInfo());
        new Gson().toJson(new Plugin());
        new Gson().toJson(new User());
        new Gson().toJson(new ChangyanComment());
        new Gson().toJson(new CommentsEntry());
        String basePath = System.getProperty("user.dir").replace("\\target","").replace("/target", "");
        //PathKit.setRootPath(basePath);
        File file = new File(basePath + "/src/main/resources");
        PluginNativeImageUtils.doLoopResourceLoad(file.listFiles(), file.getPath()  + "/", "/");
        //Application.nativeAgent = true;
        PluginNativeImageUtils.exposeController(Collections.singletonList(CommentController.class));
        Application.main(args);

    }
}