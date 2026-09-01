package com.efatha.e_support_hub_backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class Home {
    @GetMapping("/home")
    public Map<String, String> HomeController() {
      return Map.of("message", "Welcome to E-Support Hub Backend!");
    }
}
