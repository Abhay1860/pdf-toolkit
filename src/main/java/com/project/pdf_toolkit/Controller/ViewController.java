package com.project.pdf_toolkit.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/merge-page")
    public String mergePage() {
        return "merge";
    }

    @GetMapping("/split-page")
    public String splitPage() {
        return "split";
    }

    @GetMapping("/page-count-page")
    public String pageCountPage() {
        return "page-count";
    }
}