package com.rootl.flux.infrastructure.adapter.in.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebUiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET / deve carregar a interface web do console de orquestração com sucesso")
    void shouldServeDashboardHtml() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Root L Flux")))
                .andExpect(content().string(containsString("Console de Orquestra")));
    }

    @Test
    @DisplayName("GET /ui deve disponibilizar o console diretamente com status 200")
    void shouldServeUiEndpoint() throws Exception {
        mockMvc.perform(get("/ui"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Root L Flux")));
    }
}
