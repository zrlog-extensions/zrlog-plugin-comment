package com.fzb.zrlog.plugin.changyan.controller;

import org.apache.commons.fileupload.RequestContext;

import java.io.InputStream;

public class ChangyanRequestContext implements RequestContext {

    private String characterEncoding;
    private String contentType;
    private int contentLength;
    private InputStream inputStream;

    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
