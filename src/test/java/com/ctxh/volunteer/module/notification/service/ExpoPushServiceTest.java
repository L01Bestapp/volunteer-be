package com.ctxh.volunteer.module.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpoPushServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private ExpoPushService expoPushService;

    @BeforeEach
    void setUp() {
        expoPushService = new ExpoPushService(webClientBuilder);
    }

    @Test
    @DisplayName("sendNotification returns false when token is null or empty")
    void sendNotification_NullToken_ReturnsFalse() {
        assertFalse(expoPushService.sendNotification(null, "Title", "Body"));
        assertFalse(expoPushService.sendNotification("", "Title", "Body"));
    }

    @Test
    @DisplayName("sendNotification returns false when token format is invalid")
    void sendNotification_InvalidToken_ReturnsFalse() {
        assertFalse(expoPushService.sendNotification("invalid-token", "Title", "Body"));
    }

    @Test
    @DisplayName("sendNotification returns true when API returns success")
    void sendNotification_Success_ReturnsTrue() {
        // Mock WebClient chain
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Mock API response
        Map<String, Object> responseData = new HashMap<>();
        Map<String, Object> dataItem = new HashMap<>();
        dataItem.put("status", "ok");
        dataItem.put("id", "ticket-id");
        responseData.put("data", List.of(dataItem));

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(responseData));

        // Act
        boolean result = expoPushService.sendNotification("ExponentPushToken[xxx]", "Title", "Body", new HashMap<>());

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("sendNotification returns false when API returns error")
    void sendNotification_ApiError_ReturnsFalse() {
        // Mock WebClient chain
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Mock API response with error
        Map<String, Object> responseData = new HashMap<>();
        Map<String, Object> dataItem = new HashMap<>();
        dataItem.put("status", "error");
        dataItem.put("message", "Some error");
        responseData.put("data", List.of(dataItem));

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(responseData));

        // Act
        boolean result = expoPushService.sendNotification("ExponentPushToken[xxx]", "Title", "Body");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("sendBatchNotifications handles tokens correctly")
    void sendBatchNotifications_Success() {
        // Mock WebClient chain
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Mock API response
        Map<String, Object> responseData = new HashMap<>();
        Map<String, Object> successItem = new HashMap<>();
        successItem.put("status", "ok");
        responseData.put("data", List.of(successItem));

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(responseData));

        // Act
        List<String> tokens = List.of("ExponentPushToken[123]");
        Map<String, Boolean> results = expoPushService.sendBatchNotifications(tokens, "Title", "Body", null);

        // Assert
        assertTrue(results.get("ExponentPushToken[123]"));
    }
    
    @Test
    @DisplayName("sendBatchNotifications returns empty for empty token list")
    void sendBatchNotifications_EmptyList() {
        Map<String, Boolean> results = expoPushService.sendBatchNotifications(Collections.emptyList(), "Title", "Body", null);
        assertTrue(results.isEmpty());
    }
}
