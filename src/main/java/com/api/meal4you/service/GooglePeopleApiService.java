package com.api.meal4you.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;

@Service
public class GooglePeopleApiService {

    @Data
    @AllArgsConstructor
    public static class GoogleUserInfo {
        private String id;
        private String email;
        private String name;
    }

    public GoogleUserInfo getUserInfo(String accessToken) {
        try {
            URL url = new URL("https://people.googleapis.com/v1/people/me?personFields=names,emailAddresses");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("Erro ao consultar Google People API: HTTP " + responseCode);
            }

            StringBuilder response;
            try (Scanner scanner = new Scanner(conn.getInputStream())) {
                response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());

            String id = null;
            String email = null;
            String name = null;

            // Extrai o nome e ID
            if (root.has("names") && root.get("names").isArray() && root.get("names").size() > 0) {
                name = root.get("names").get(0).get("givenName").asText();
                id = root.get("names").get(0).get("metadata").get("source").get("id").asText();
            }
            // Extrai o e-mail
            if (root.has("emailAddresses") && root.get("emailAddresses").isArray() && root.get("emailAddresses").size() > 0) {
                email = root.get("emailAddresses").get(0).get("value").asText();
            }

            if (id == null || email == null || name == null) {
                throw new RuntimeException("Não foi possível extrair informações do usuário do Google.");
            }

            return new GoogleUserInfo(id, email, name);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao consultar Google People API: " + e.getMessage(), e);
        }
    }
}
