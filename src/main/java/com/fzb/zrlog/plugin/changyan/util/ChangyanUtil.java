package com.fzb.zrlog.plugin.changyan.util;

import com.fzb.zrlog.plugin.common.modle.Comment;
import com.fzb.zrlog.plugin.changyan.response.ChangyanComment;
import com.fzb.zrlog.plugin.changyan.response.Meta;
import com.fzb.zrlog.plugin.changyan.response.ResponseEntry;
import com.fzb.zrlog.plugin.changyan.response.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class ChangyanUtil {

    public static String hmacSHA1Encrypt(Map<String, Object> param, String encryptKey) throws Exception {
        return hmacSHA1Encrypt(ParseTools.mapToQueryStr(param), encryptKey);
    }

    private static String hmacSHA1Encrypt(String encryptText, String encryptKey) throws Exception {
        String HMAC_SHA1_ALGORITHM = "HmacSHA1";
        SecretKeySpec signingKey = new SecretKeySpec(encryptKey.getBytes(), HMAC_SHA1_ALGORITHM);
        // Get an hmac_sha1 Mac instance and initialise with the signing key
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        // Compute the hmac
        byte[] rawHmac = mac.doFinal(encryptText.getBytes());
        byte[] hexBytes = new Hex().encode(rawHmac);
        byte hex[] = hexString2Bytes(new String(hexBytes, "ISO-8859-1"));
        return new String(Base64.encodeBase64(hex));
    }


    public static List<ResponseEntry> getComments(String shortName, String secret) {
        ChangyanComment changyanComment = getComment(10000, shortName, secret);
        if (changyanComment.getCode() == 0) {
            return changyanComment.getResponse();
        }
        return new ArrayList<ResponseEntry>();
    }

    public static ResponseEntry getCommentLast(String shortName, String secret) {
        ChangyanComment changyanComment = getComment(1, shortName, secret);
        if (changyanComment.getCode() == 0) {
            return changyanComment.getResponse().get(0);
        }
        return null;
    }

    private static ChangyanComment getComment(int limit, String shortName, String secret) {
        String urlPath = "http://api.changyan.com/log/list.json";
        Map<String, Object> params = new HashMap<>();
        params.put("short_name", shortName);
        params.put("secret", secret);
        params.put("limit", limit);
        params.put("order", "desc");
        try {
            return new Gson().fromJson(HttpUtil.getResponse(urlPath, params),
                    new TypeToken<ChangyanComment>() {
                    }.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ChangyanComment();
    }

    private static User getAvatarUrl(String userId) {
        String urlPath = "http://api.changyan.com/users/profile.json";
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);
        try {
            return (new Gson().fromJson(HttpUtil.getResponse(urlPath, params),
                    new TypeToken<User>() {
                    }.getType()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] hexString2Bytes(String hexStr) {
        byte[] b = new byte[hexStr.length() / 2];
        int j = 0;
        for (int i = 0; i < b.length; i++) {
            char c0 = hexStr.charAt(j++);
            char c1 = hexStr.charAt(j++);
            b[i] = (byte) ((parse(c0) << 4) | parse(c1));
        }
        return b;
    }

    private static int parse(char c) {
        if (c >= 'a')
            return (c - 'a' + 10) & 0x0F;
        if (c >= 'A')
            return (c - 'A' + 10) & 0x0F;
        return (c - '0') & 0x0F;
    }

    public static Comment convertToSelf(Meta meta) {
        Date createdTime = null;
        try {
            createdTime = ParseTools.getDataBySdf("yyyy-MM-dd HH:mm:ss", meta.getCreated_at());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Comment comment = new Comment();
        comment.setIp(meta.getIp());
        comment.setName(meta.getAuthor_name());
        comment.setMail(meta.getAuthor_email());
        comment.setCreatedTime(createdTime);
        comment.setContent(meta.getMessage());
        comment.setLogId(meta.getThread_key());
        comment.setPostId(meta.getPost_id());
        comment.setHome(meta.getAuthor_url());
        User user = getAvatarUrl(meta.getAuthor_id() + "");
        if (user != null && user.getCode() == 0) {
            comment.setHeadPortrait(user.getResponse().getAvatar_url());
        }
        return comment;
    }
}
