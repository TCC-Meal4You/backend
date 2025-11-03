package com.api.meal4you.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.api.meal4you.security.JwtAuthFilter;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        //Usuario
                        .requestMatchers(HttpMethod.POST, "/usuarios/login").permitAll() // login
                        .requestMatchers(HttpMethod.POST, "/usuarios/login/oauth2/google").permitAll() // login social
                        .requestMatchers(HttpMethod.POST, "/usuarios").permitAll() // cadastro
                        .requestMatchers(HttpMethod.POST, "/usuarios/verifica-email").permitAll() // verifica email
                        .requestMatchers("/usuarios/**").hasRole("USUARIO") // outros métodos
                        
                        //Admin 
                        .requestMatchers(HttpMethod.POST, "/admins/login").permitAll() // login
                        .requestMatchers(HttpMethod.POST, "/admins/login/oauth2/google").permitAll() // login social
                        .requestMatchers(HttpMethod.POST, "/admins").permitAll() // cadastro
                        .requestMatchers(HttpMethod.POST, "/admins/verifica-email").permitAll() // verifica email
                        .requestMatchers("/admins/**").hasRole("ADMIN") // outros métodos
                        
                        //Restaurante
                        .requestMatchers(HttpMethod.GET, "/restaurantes").hasRole("USUARIO") // listar
                        .requestMatchers(HttpMethod.GET, "/restaurantes/listar-por-id/{id}").hasRole("USUARIO") // listar por id
                        .requestMatchers(HttpMethod.POST, "/restaurantes").hasRole("ADMIN") // cadastrar
                        .requestMatchers(HttpMethod.PUT, "/restaurantes/{id}").hasRole("ADMIN") // atualizar
                        .requestMatchers(HttpMethod.DELETE, "/restaurantes/{id}").hasRole("ADMIN") // deletar
                        .requestMatchers(HttpMethod.GET, "/restaurantes/meu-restaurante").hasRole("ADMIN") // meu restaurante

                        //Refeições
                        .requestMatchers("/refeicoes/**").hasRole("ADMIN") // todos os métodos

                        //Ingredientes
                        .requestMatchers("/ingredientes/**").hasRole("ADMIN") // todos os métodos

                        //Restrições
                        .requestMatchers(HttpMethod.GET, "/restricoes").hasAnyRole("ADMIN", "USUARIO") // listar
                        .requestMatchers(HttpMethod.POST, "/restricoes/sincronizar").permitAll() // sincronizar IA

                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> {})
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}