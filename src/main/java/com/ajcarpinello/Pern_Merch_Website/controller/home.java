package com.ajcarpinello.Pern_Merch_Website.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class home {

    @GetMapping("/")
    public String hello() {
        return "hello";
    }
}
