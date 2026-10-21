package com.digitalmoneyhouse.common.config;

import com.digitalmoneyhouse.common.exception.DownstreamServiceException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class FeignErrorDecoderTest {

    private final FeignErrorDecoder decoder = new FeignErrorDecoder();

    @Test
    void preservesTheStatusAndMessageReturnedByTheDownstreamService() {
        Response response = response(404, "Usuario no encontrado");

        Exception exception = decoder.decode("UsersClient#get", response);

        DownstreamServiceException translated = assertInstanceOf(
            DownstreamServiceException.class,
            exception
        );
        assertEquals(404, translated.getStatus());
        assertEquals("Usuario no encontrado", translated.getMessage());
    }

    @Test
    void hidesMessagesFromUnavailableDownstreamServices() {
        Response response = response(503, "detalle interno");

        Exception exception = decoder.decode("UsersClient#get", response);

        DownstreamServiceException translated = assertInstanceOf(
            DownstreamServiceException.class,
            exception
        );
        assertEquals(503, translated.getStatus());
        assertEquals(
            "El servicio requerido no está disponible",
            translated.getMessage()
        );
    }

    private Response response(int status, String message) {
        return Response.builder()
            .status(status)
            .reason("Error")
            .request(Request.create(
                Request.HttpMethod.GET,
                "http://users-service/api/users/test",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                null
            ))
            .body(
                "{\"message\":\"" + message + "\"}",
                StandardCharsets.UTF_8
            )
            .build();
    }
}
