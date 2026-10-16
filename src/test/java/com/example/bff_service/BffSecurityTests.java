package com.example.bff_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class BffSecurityTests {

    private static final SimpleGrantedAuthority SCOPE = new SimpleGrantedAuthority("SCOPE_Pedidos.Access");
    private static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_Admin");

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mvc.perform(get("/api/pedidos")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSinScopeDevuelve403() throws Exception {
        mvc.perform(get("/api/pedidos").with(jwt())).andExpect(status().isForbidden());
    }

    @Test
    void conScopeReenviaYSiElServicioEstaCaidoDevuelve502() throws Exception {
        mvc.perform(get("/api/pedidos").with(jwt().authorities(SCOPE)))
                .andExpect(status().isBadGateway());
    }

    @Test
    void eliminarSinRolAdminDevuelve403() throws Exception {
        mvc.perform(delete("/api/pedidos/1").with(jwt().authorities(SCOPE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void eliminarConRolAdminPasaLaSeguridad() throws Exception {
        // 502 porque pedidos-service esta caido en el test; lo importante es que NO sea 401/403
        mvc.perform(delete("/api/pedidos/1").with(jwt().authorities(SCOPE, ADMIN)))
                .andExpect(status().isBadGateway());
    }
}
