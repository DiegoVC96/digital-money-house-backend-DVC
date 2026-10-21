package com.digitalmoneyhouse.common.config;

import com.digitalmoneyhouse.common.exception.DownstreamServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeignErrorDecoder implements ErrorDecoder {

    private static final Pattern MESSAGE_PATTERN = Pattern.compile(
        "\\\"message\\\"\\s*:\\s*\\\"([^\\\"]*)\\\""
    );

    @Override
    public Exception decode(String methodKey, Response response) {
        return new DownstreamServiceException(
            response.status(),
            messageFrom(response)
        );
    }

    private String messageFrom(Response response) {
        if (response.status() >= 500) {
            return "El servicio requerido no está disponible";
        }

        if (response.body() == null) {
            return "El servicio requerido rechazó la solicitud";
        }

        try {
            String body = new String(
                response.body().asInputStream().readAllBytes(),
                StandardCharsets.UTF_8
            );
            Matcher matcher = MESSAGE_PATTERN.matcher(body);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (IOException exception) {
            // Se mantiene un mensaje seguro si el cuerpo no puede leerse.
        }

        return "El servicio requerido rechazó la solicitud";
    }
}
