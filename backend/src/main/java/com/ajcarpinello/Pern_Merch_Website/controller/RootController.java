package com.ajcarpinello.Pern_Merch_Website.controller;

@RestController
public class RootController {
    @GetMapping("/")
    public String root() { return "OK"; }
}