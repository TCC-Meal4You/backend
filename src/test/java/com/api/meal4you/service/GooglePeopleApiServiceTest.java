package com.api.meal4you.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GooglePeopleApiServiceTest {

    @Test
    void shouldParseGoogleUserInfoFromResponse() throws IOException {
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getResponseCode()).thenReturn(200);
        String payload = "{\"names\":[{\"givenName\":\"Gabri\",\"metadata\":{\"source\":{\"id\":\"123\"}}}],\"emailAddresses\":[{\"value\":\"gabri@example.com\"}]}";
        when(connection.getInputStream()).thenReturn(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)));

        GooglePeopleApiService service = new GooglePeopleApiService(url -> connection);

        GooglePeopleApiService.GoogleUserInfo info = service.getUserInfo("token");

        assertThat(info.getId()).isEqualTo("123");
        assertThat(info.getEmail()).isEqualTo("gabri@example.com");
        assertThat(info.getName()).isEqualTo("Gabri");
        verify(connection).setRequestMethod("GET");
        verify(connection).setRequestProperty("Authorization", "Bearer token");
    }

    @Test
    void shouldThrowWhenGoogleApiReturnsNonOk() throws IOException {
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getResponseCode()).thenReturn(401);

        GooglePeopleApiService service = new GooglePeopleApiService(url -> connection);

        assertThatThrownBy(() -> service.getUserInfo("token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("HTTP 401");
    }
}