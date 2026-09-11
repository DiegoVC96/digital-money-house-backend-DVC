package com.digitalmoneyhouse.auth.service;

import com.digitalmoneyhouse.auth.domain.VerificationCode;
import com.digitalmoneyhouse.auth.domain.VerificationPurpose;
import com.digitalmoneyhouse.auth.repository.VerificationCodeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.digitalmoneyhouse.common.exception.InvalidVerificationCodeException;
import java.util.Optional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Service
@Transactional
public class VerificationCodeService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long expirationMinutes;
    private final String mailFrom;

    public VerificationCodeService(
        VerificationCodeRepository verificationCodeRepository,
        JavaMailSender mailSender,
        PasswordEncoder passwordEncoder,
        @Value("${verification.code-expiration-minutes}")
        long expirationMinutes,
        @Value("${verification.mail-from}") String mailFrom
    ) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
        this.expirationMinutes = expirationMinutes;
        this.mailFrom = mailFrom;
    }

    public void issueCode(
        String email,
        VerificationPurpose purpose
    ) {
        String normalizedEmail = email.toLowerCase(Locale.ROOT);

        verificationCodeRepository
            .findByEmailIgnoreCaseAndPurposeAndUsedAtIsNull(
                normalizedEmail,
                purpose
            )
            .forEach(VerificationCode::markAsUsed);

        String plainCode = String.format(
            "%06d",
            secureRandom.nextInt(1_000_000)
        );

        VerificationCode verificationCode = new VerificationCode(
            normalizedEmail,
            passwordEncoder.encode(plainCode),
            purpose,
            Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES)
        );

        verificationCodeRepository.save(verificationCode);
        sendEmail(normalizedEmail, plainCode, purpose);
    }

    public void verifyCode(
        String email,
        String plainCode,
        VerificationPurpose purpose
    ) {
        String normalizedEmail = email.toLowerCase(Locale.ROOT);

        Optional<VerificationCode> latestCode =
            verificationCodeRepository
                .findTopByEmailIgnoreCaseAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(
                    normalizedEmail,
                    purpose
                );

        VerificationCode verificationCode = latestCode.orElseThrow(
            () -> new InvalidVerificationCodeException(
                "Código inválido o vencido"
            )
        );

        if (
            !verificationCode.isActive(Instant.now())
                || !passwordEncoder.matches(
                    plainCode,
                    verificationCode.getCodeHash()
                )
        ) {
            throw new InvalidVerificationCodeException(
                "Código inválido o vencido"
            );
        }

        verificationCode.markAsUsed();
    }

    private void sendEmail(
        String email,
        String plainCode,
        VerificationPurpose purpose
    ) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject(subjectFor(purpose));
        message.setText(
            "Tu código de Digital Money House es: "
                + plainCode
                + "\n\nVence en "
                + expirationMinutes
                + " minutos."
        );

        mailSender.send(message);
    }

    private String subjectFor(VerificationPurpose purpose) {
        return switch (purpose) {
            case EMAIL_VERIFICATION ->
                "Verifica tu correo electrónico";
            case PASSWORD_RESET ->
                "Recuperación de contraseña";
        };
    }
}