package com.anoyi.douyin.service.impl;

import com.alibaba.fastjson.JSON;
import com.anoyi.douyin.bean.DyUserVO;
import com.anoyi.douyin.entity.DyAweme;
import com.anoyi.douyin.rpc.SignServiceGrpc;
import com.anoyi.douyin.rpc.WebSignRequest;
import com.anoyi.douyin.service.DouyinService;
import com.anoyi.douyin.util.DyNumberConvertor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
// @GrpcService
public class DouyinServiceImpl implements DouyinService {

    private final static String UserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1";

    private final static String XMLHttpRequest = "XMLHttpRequest";

    private final static String VIDEO_LIST_API = "https://www.iesdouyin.com/aweme/v1/aweme/post/?user_id=%s&count=21&max_cursor=%s&aid=1128&_signature=%s&dytk=%s";

    private final static String USER_SHARE_API = "https://www.iesdouyin.com/share/user/%s?share_type=link";

    @GrpcClient("sign-service")
    private SignServiceGrpc.SignServiceBlockingStub signServiceBlockingStub;

    /**
     * 确保接口返回结果
     */
    public DyAweme videoList(String dyId, String dytk, String cursor) {
        DyAweme videos = getVideoList(dyId, dytk, cursor);
        while (videos.getHas_more() == 1 && CollectionUtils.isEmpty(videos.getAweme_list())) {
            videos = getVideoList(dyId, dytk, cursor);
        }
        return videos;
    }

    /**
     * 获取抖音用户视频列表
     */
    public DyAweme getVideoList(String dyId, String dytk, String cursor) {
        WebSignRequest request = WebSignRequest.newBuilder().setUid(dyId).build();
        String signature = signServiceBlockingStub.webSign(request).getSignature();
        String api = String.format(VIDEO_LIST_API, dyId, cursor, signature, dytk);
        try {
            Document document = httpGet(api);
            return JSON.parseObject(document.text(), DyAweme.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("HTTP request error: " + api);
    }

    /**
     * 获取抖音用户信息
     */
    public DyUserVO getDyUser(String dyId) {
        String api = String.format(USER_SHARE_API, dyId);
        try {
            DyUserVO dyUser = new DyUserVO();
            dyUser.setId(dyId);
            Document document = httpGet(api);
            parseIconFonts(document);
            String nickname = document.select("p.nickname").text();
            dyUser.setNickname(nickname);
            String avatar = document.select("img.avatar").attr("src");
            dyUser.setAvatar(avatar);
            String tk = match(document.html(), "dytk: '(.*?)'");
            dyUser.setTk(tk);
            String shortId = document.select("p.shortid").text();
            dyUser.setShortId(shortId);
            String verifyInfo = document.select("div.verify-info").text();
            dyUser.setVerifyInfo(verifyInfo);
            String signature = document.select("p.signature").text();
            dyUser.setSignature(signature);
            String location = document.select("span.location").text();
            dyUser.getExtraInfo().put("location", location);
            String constellation = document.select("span.constellation").text();
            dyUser.getExtraInfo().put("constellation", constellation);
            String focus = document.select("span.focus.block span.num").text();
            dyUser.getFollowInfo().put("focus", focus);
            String follower = document.select("span.follower.block span.num").text();
            dyUser.getFollowInfo().put("follower", follower);
            String likeNum = document.select("span.liked-num.block span.num").text();
            dyUser.getFollowInfo().put("likeNum", likeNum);
            DyAweme videos = videoList(dyId, tk, "0");
            dyUser.setVideos(videos);
            return dyUser;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("HTTP request error: " + api);
    }

    /**
     * HTTP 请求
     */
    private Document httpGet(String url) throws IOException {
        Connection.Response response = Jsoup.connect(url)
                                            .header("user-agent", UserAgent)
                                            .header("x-requested-with", XMLHttpRequest)
                                            .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                                            .ignoreContentType(true).execute();
        String html = response.body().replace("&#xe", "");
        return Jsoup.parse(html);
    }

    /**
     * 正则匹配
     */
    private String match(String content, String regx) {
        Matcher matcher = Pattern.compile(regx).matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * 全局 icon 数字解析
     */
    private void parseIconFonts(Document document) {
        Elements elements = document.select("i.icon.iconfont");
        elements.forEach(element -> {
            String text = element.text();
            String number = DyNumberConvertor.getNumber(text);
            element.text(number);
        });
    }

}
