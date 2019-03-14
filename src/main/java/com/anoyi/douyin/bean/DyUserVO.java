package com.anoyi.douyin.bean;

import com.anoyi.douyin.entity.DyAweme;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class DyUserVO {

    private String id;

    private String tk;

    private String avatar;

    private String nickname;

    private String shortId;

    private String signature;

    private String verifyInfo;

    private Map<String, String> extraInfo = new HashMap<>();

    private Map<String, String> followInfo = new HashMap<>();

    private DyAweme videos;

}
