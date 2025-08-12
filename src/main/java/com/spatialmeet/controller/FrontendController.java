package com.spatialmeet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping("/")
    public String index() {
        return "index.html";
    }
    
    @GetMapping("/app")
    public String app() {
        return "index.html";
    }
}
