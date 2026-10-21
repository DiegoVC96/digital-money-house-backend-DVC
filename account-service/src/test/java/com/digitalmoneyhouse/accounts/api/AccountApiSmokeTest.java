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
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;

class AccountApiSmokeTest {

    private static String email;
    private static String password;
    private static String cardId;
    private static String otherAccountId;
    private static String recipientAlias;
    private static String accessToken;
    private static String userId;
    private static String accountId;
    private static String accountAlias;

    @BeforeAll
    static void configure() throws Exception {
        String baseUrl = configuration(
            "smoke.base-url",
            "SMOKE_BASE_URL"
        );

        email = configuration("smoke.email", "SMOKE_EMAIL");
        password = configuration("smoke.password", "SMOKE_PASSWORD");
        cardId = configuration("smoke.card-id", "SMOKE_CARD_ID");
        otherAccountId = configuration(
            "smoke.other-account-id",
            "SMOKE_OTHER_ACCOUNT_ID"
        );
        recipientAlias = configuration(
            "smoke.recipient-alias",
            "SMOKE_RECIPIENT_ALIAS"
        );

        Assumptions.assumeTrue(
            !baseUrl.isBlank() && !email.isBlank() && !password.isBlank()
                && !cardId.isBlank(),
            "Definí SMOKE_BASE_URL, SMOKE_EMAIL, SMOKE_PASSWORD y SMOKE_CARD_ID"
        );

        RestAssured.baseURI = baseUrl;
        accessToken = login();
        userId = jwtSubject(accessToken);
        var account = given()
            .auth()
            .oauth2(accessToken)
            .when()
            .get("/api/accounts/me/{userId}", userId)
            .then()
            .statusCode(200)
            .extract();

        accountId = account.path("id");
        accountAlias = account.path("alias");
    }

    @Test
    void authenticatedUserCanReadOwnDigitalAccount() throws Exception {
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

    @Test
    void authenticatedUserCanReadActivityWithoutFilters() {
        given()
            .auth()
            .oauth2(accessToken)
            .when()
            .get("/api/accounts/{accountId}/activity", accountId)
            .then()
            .statusCode(200);
    }

    @Test
    void canDepositMoneyAndReadItsActivityDetail() {
        String transactionId = given()
            .auth()
            .oauth2(accessToken)
            .contentType(ContentType.JSON)
            .body(Map.of("cardId", cardId, "amount", 1.00))
            .when()
            .post("/api/accounts/{accountId}/transferences", accountId)
            .then()
            .statusCode(201)
            .body("type", equalTo("DEPOSIT"))
            .body("id", notNullValue())
            .extract()
            .path("id");

        given()
            .auth()
            .oauth2(accessToken)
            .when()
            .get(
                "/api/accounts/{accountId}/activity/{transactionId}",
                accountId,
                transactionId
            )
            .then()
            .statusCode(200)
            .body("id", equalTo(transactionId))
            .body("type", equalTo("DEPOSIT"));
    }

    @Test
    void rejectsInvalidDepositAmount() {
        given()
            .auth()
            .oauth2(accessToken)
            .contentType(ContentType.JSON)
            .body(Map.of("cardId", cardId, "amount", 0))
            .when()
            .post("/api/accounts/{accountId}/transferences", accountId)
            .then()
            .statusCode(400);
    }

    @Test
    void returnsNotFoundForUnknownActivityDetail() {
        given()
            .auth()
            .oauth2(accessToken)
            .when()
            .get(
                "/api/accounts/{accountId}/activity/{transactionId}",
                accountId,
                UUID.randomUUID()
            )
            .then()
            .statusCode(404);
    }

    @Test
    void rejectsActivityFromAnotherAccountWhenConfigured() {
        Assumptions.assumeTrue(
            !otherAccountId.isBlank(),
            "Definí SMOKE_OTHER_ACCOUNT_ID para validar el caso 403"
        );

        given()
            .auth()
            .oauth2(accessToken)
            .when()
            .get("/api/accounts/{accountId}/activity", otherAccountId)
            .then()
            .statusCode(403);
    }

    @Test
    void rejectsTransferWithoutAnAccessToken() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("destination", accountAlias, "amount", 1))
            .when()
            .post("/api/accounts/{accountId}/transferences", accountId)
            .then()
            .statusCode(401);
    }

    @Test
    void rejectsTransferToTheSameAccount() {
        given()
            .auth()
            .oauth2(accessToken)
            .contentType(ContentType.JSON)
            .body(Map.of("destination", accountAlias, "amount", 1))
            .when()
            .post("/api/accounts/{accountId}/transferences", accountId)
            .then()
            .statusCode(400);
    }

    @Test
    void rejectsTransferWithAnInvalidAmount() {
        given()
            .auth()
            .oauth2(accessToken)
            .contentType(ContentType.JSON)
            .body(Map.of("destination", accountAlias, "amount", 0))
            .when()
            .post("/api/accounts/{accountId}/transferences", accountId)
            .then()
            .statusCode(400);
    }

    @Test
    void rejectsTransferToAnUnknownDestination() {
        given()
            .auth()
            .oauth2(accessToken)
            .contentType(ContentType.JSON)
            .body(Map.of("destination", "nunca.existe.cuenta", "amount", 1))
            .when()
            .post("/api/accounts/{accountId}/transferences", accountId)
            .then()
            .statusCode(400);
    }

    @Test
    void transfersToConfiguredRecipientAndListsItAsRecent() {
        Assumptions.assumeTrue(
            !recipientAlias.isBlank(),
            "Definí SMOKE_RECIPIENT_ALIAS para validar transferencias"
        );

        given()
            .auth()
            .oauth2(accessToken)
            .contentType(ContentType.JSON)
            .body(Map.of("cardId", cardId, "amount", 1.00))
            .when()
            .post("/api/accounts/{accountId}/transferences", accountId)
            .then()
            .statusCode(201);

        given()
            .auth()
            .oauth2(accessToken)
            .contentType(ContentType.JSON)
            .body(Map.of("destination", recipientAlias, "amount", 1.00))
            .when()
            .post("/api/accounts/{accountId}/transferences", accountId)
            .then()
            .statusCode(200)
            .body("type", equalTo("TRANSFER_OUT"));

        given()
            .auth()
            .oauth2(accessToken)
            .when()
            .get("/api/accounts/{accountId}/transferences", accountId)
            .then()
            .statusCode(200)
            .body("alias", org.hamcrest.Matchers.hasItem(recipientAlias));
    }

    @Test
    void rejectsTransferWithInsufficientFundsWhenRecipientIsConfigured() {
        Assumptions.assumeTrue(
            !recipientAlias.isBlank(),
            "Definí SMOKE_RECIPIENT_ALIAS para validar fondos insuficientes"
        );

        given()
            .auth()
            .oauth2(accessToken)
            .contentType(ContentType.JSON)
            .body(Map.of("destination", recipientAlias, "amount", 999999999.99))
            .when()
            .post("/api/accounts/{accountId}/transferences", accountId)
            .then()
            .statusCode(410);
    }

    private static String login() {
        return given()
            .contentType(ContentType.JSON)
            .body(Map.of("email", email, "password", password))
            .when()
            .post("/api/auth/login")
            .then()
            .statusCode(200)
            .extract()
            .path("accessToken");
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
