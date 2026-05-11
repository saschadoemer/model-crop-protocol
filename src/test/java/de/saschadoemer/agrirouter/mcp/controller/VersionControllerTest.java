package de.saschadoemer.agrirouter.mcp.controller;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
public class VersionControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void testVersion() {
        String response = client.toBlocking().retrieve(HttpRequest.GET("/version"));
        assertEquals("1.0.0", response);
    }
}
