package com.fzb.zrlog.plugin.render;

import com.fzb.zrlog.plugin.message.Plugin;

import java.io.InputStream;
import java.util.Map;

public class TemplateRender implements IRenderHandler {

    @Override
    public String render(String s, Plugin plugin, Map<String, Object> map) {
        return render(TemplateRender.class.getResourceAsStream(s), plugin, map);
    }

    @Override
    public String render(InputStream inputStream, Plugin plugin, Map<String, Object> map) {
        String renderResult = com.fzb.common.util.IOUtil.getStringInputStream(inputStream);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            renderResult = renderResult.replace("${" + entry.getKey() + "}", entry.getValue().toString());
        }
        return renderResult;
    }
}
