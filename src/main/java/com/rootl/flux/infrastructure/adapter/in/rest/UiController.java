package com.rootl.flux.infrastructure.adapter.in.rest;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class UiController {

    @GetMapping(value = {"/", "/ui", "/dashboard"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> dashboard() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/index.html");
        String html = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/html;charset=UTF-8"))
                .body(html);
    }
}
