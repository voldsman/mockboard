package dev.mockboard.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/hello")
public class HelloController {

    @GetMapping
    public Map<String, String> hello() {
        return Map.of("message", "Hello World!");
    }
}
