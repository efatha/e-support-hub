package com.efatha.e_support_hub_backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Home {
    
    @GetMapping("/home")
    public String HomeController(){
      return "Welcome to E-Support Hub Backend!";
    }
}
