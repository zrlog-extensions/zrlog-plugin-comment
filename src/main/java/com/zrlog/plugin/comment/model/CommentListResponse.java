package com.zrlog.plugin.comment.model;

import java.util.Collections;
import java.util.List;

public class CommentListResponse {

    private List<CommentRenderModel> data;

    public List<CommentRenderModel> getData() {
        return data == null ? Collections.emptyList() : data;
    }

    public void setData(List<CommentRenderModel> data) {
        this.data = data;
    }
}
