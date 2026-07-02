package com.zrlog.plugin.comment;

import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.type.RunType;
import com.zrlog.plugin.comment.config.ChangyanConfig;
import com.zrlog.plugin.comment.config.CommentApiResponse;
import com.zrlog.plugin.comment.config.CommentBaseConfig;
import com.zrlog.plugin.comment.config.CommentHistoryConfig;
import com.zrlog.plugin.comment.config.CommentHistoryRecord;
import com.zrlog.plugin.comment.config.CommentSubmitRequest;
import com.zrlog.plugin.comment.config.CommentUpdateRequest;
import com.zrlog.plugin.comment.config.CommentWebsiteConfig;
import com.zrlog.plugin.comment.config.WebsiteKeyRequest;
import com.zrlog.plugin.comment.controller.CommentController;
import com.zrlog.plugin.comment.response.ChangyanComment;
import com.zrlog.plugin.comment.response.CommentsEntry;
import com.zrlog.plugin.comment.response.User;
import com.zrlog.plugin.common.PluginNativeImageUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class GraalvmAgentApplication {


    public static void main(String[] args) throws IOException {
        RunConstants.runType = RunType.AGENT;
        PluginNativeImageUtils.usedGsonObject();
        PluginNativeImageUtils.gsonNativeAgentByClazz(Arrays.asList(ChangyanComment.class, CommentsEntry.class, User.class,
                ChangyanConfig.class, CommentApiResponse.class, CommentBaseConfig.class, CommentHistoryConfig.class,
                CommentHistoryRecord.class, CommentSubmitRequest.class, CommentUpdateRequest.class, CommentWebsiteConfig.class,
                WebsiteKeyRequest.class));
        String basePath = System.getProperty("user.dir").replace("\\target", "").replace("/target", "");
        //PathKit.setRootPath(basePath);
        File file = new File(basePath + "/src/main/resources");
        PluginNativeImageUtils.doLoopResourceLoad(file.listFiles(), file.getPath() + "/", "/");
        //Application.nativeAgent = true;
        PluginNativeImageUtils.exposeController(Collections.singletonList(CommentController.class));
        Application.main(args);

    }
}
