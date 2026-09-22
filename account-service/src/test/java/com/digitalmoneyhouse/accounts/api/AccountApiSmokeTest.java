package com.digitalmoneyhouse.accounts.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;

class AccountApiSmokeTest {

    private static String email;
    private static String password;

    @BeforeAll
    static void configure() {
        String baseUrl = configuration(
            "smoke.base-url",
            "SMOKE_BASE_URL"
        );

        email = configuration("smoke.email", "SMOKE_EMAIL");
        password = configuration("smoke.password", "SMOKE_PASSWORD");

        Assumptions.assumeTrue(
            !baseUrl.isBlank() && !email.isBlank() && !password.isBlank(),
            "Definí SMOKE_BASE_URL, SMOKE_EMAIL y SMOKE_PASSWORD"
        );

        RestAssured.baseURI = baseUrl;
    }

    @Test
    void authenticatedUserCanReadOwnDigitalAccount() throws Exception {
        String accessToken = given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                "email", email,
                "password", password
            ))
            .when()
            .post("/api/auth/login")
            .then()
            .statusCode(200)
            .extract()
            .path("accessToken");

        String userId = jwtSubject(accessToken);

        given()
            .auth()
            .oauth2(accessToken)
            .when()
            .get("/api/accounts/me/{userId}", userId)
            .then()
            .statusCode(200)
            .body("userId", equalTo(userId))
            .body("cvu", matchesPattern("\\d{22}"))
            .body(
                "alias",
                matchesPattern("[a-z]+\\.[a-z]+\\.[a-z]+")
            );
    }

    private static String jwtSubject(String accessToken) throws Exception {
        String payload = accessToken.split("\\.")[1];

        byte[] decodedPayload = Base64.getUrlDecoder().decode(payload);

        return new ObjectMapper()
            .readTree(new String(decodedPayload, StandardCharsets.UTF_8))
            .path("sub")
            .asText();
    }

    private static String configuration(
        String propertyName,
        String environmentName
    ) {
        String value = System.getProperty(propertyName);

        if (value == null || value.isBlank()) {
            value = System.getenv(environmentName);
        }

        return value == null ? "" : value;
    }
}