package com.anoyi.douyin.controller;

import com.anoyi.douyin.bean.DyUserVO;
import com.anoyi.douyin.entity.DyAweme;
import com.anoyi.douyin.service.DouyinService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/douyin")
@AllArgsConstructor
@CrossOrigin("*")
public class DouyinController {

    private final DouyinService douyinService;

    @GetMapping("/user/{id}")
    public DyUserVO user(@PathVariable("id") String id) {
        return douyinService.getDyUser(id);
    }

    @GetMapping("/videos/{id}/{tk}")
    public DyAweme videos(@PathVariable("id") String id,
                          @PathVariable("tk") String tk,
                          @RequestParam(value = "cursor", defaultValue = "0") String cursor) {
        return douyinService.videoList(id, tk, cursor);
    }

}