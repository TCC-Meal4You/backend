package com.api.meal4you.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.api.meal4you.controller.RestauranteController;
import com.api.meal4you.controller.UsuarioController;
import com.api.meal4you.dto.LoginResponseDTO;
import com.api.meal4you.dto.UsuarioResponseDTO;
import com.api.meal4you.config.SecurityConfig;
import com.api.meal4you.service.RestauranteService;
import com.api.meal4you.service.RefeicaoService;
import com.api.meal4you.service.UsuarioService;
import com.api.meal4you.service.ViaCepService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {UsuarioController.class, RestauranteController.class})
@Import({SecurityConfig.class, JwtAuthFilter.class})
class AuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private RefeicaoService refeicaoService;

    @MockBean
    private RestauranteService restauranteService;

    @MockBean
    private ViaCepService viaCepService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private TokenStore tokenStore;

    @Test
    void shouldAllowPublicLoginEndpointWithoutAuthorizationHeader() throws Exception {
        when(usuarioService.fazerLogin(any())).thenReturn(new LoginResponseDTO(1, "Nome", "user@example.com", "token"));

        mockMvc.perform(post("/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.api.meal4you.dto.LoginRequestDTO("user@example.com", "senha"))))
            .andExpect(status().isOk());
    }

    @Test
    void shouldRejectProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/usuarios"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAuthorizeUserProtectedEndpointWhenTokenIsValid() throws Exception {
        String token = "valid-user-token";

        when(jwtUtil.tokenValido(token)).thenReturn(true);
        when(tokenStore.tokenEhRegistradoAtivo(token)).thenReturn(true);
        when(jwtUtil.extrairEmail(token)).thenReturn("user@example.com");
        when(jwtUtil.extrairRole(token)).thenReturn("USUARIO");
        when(usuarioService.buscarMeuPerfil()).thenReturn(new UsuarioResponseDTO("Nome", "user@example.com", Collections.emptyList()));

        mockMvc.perform(get("/usuarios")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void shouldBlockAdminEndpointWhenUserRoleIsInsufficient() throws Exception {
        String token = "user-token";

        when(jwtUtil.tokenValido(token)).thenReturn(true);
        when(tokenStore.tokenEhRegistradoAtivo(token)).thenReturn(true);
        when(jwtUtil.extrairEmail(token)).thenReturn("user@example.com");
        when(jwtUtil.extrairRole(token)).thenReturn("USUARIO");

        mockMvc.perform(post("/restaurantes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        String token = "invalid-token-for-auth";

        when(jwtUtil.tokenValido(token)).thenReturn(false);

        mockMvc.perform(get("/usuarios")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized());
    }
}
