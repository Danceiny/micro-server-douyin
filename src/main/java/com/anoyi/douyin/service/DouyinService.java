package com.anoyi.douyin.service;

import com.anoyi.douyin.bean.DyUserVO;
import com.anoyi.douyin.entity.DyAweme;

public interface DouyinService {
    public DyAweme videoList(String dyId, String dytk, String cursor);
    DyAweme getVideoList(String dyId, String dytk, String cursor);
    DyUserVO getDyUser(String dyId);
}
