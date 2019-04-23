package com.anoyi.douyin.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@AllArgsConstructor
public class HomeController {

    @GetMapping("/")
    public String home(Principal principal) {
        if (principal == null) {
            return "[micro-douyin] Hello, Anonymous!";
        }
        return "[micro-douyin] Hello, " + principal.getName() + "!";
    }

}