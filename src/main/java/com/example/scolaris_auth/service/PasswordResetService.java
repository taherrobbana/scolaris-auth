package com.example.scolaris_auth.service;

import com.example.scolaris_auth.exception.AppException;
import com.example.scolaris_auth.model.PasswordResetToken;
import com.example.scolaris_auth.model.User;
import com.example.scolaris_auth.repository.PasswordResetTokenRepository;
import com.example.scolaris_auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final JavaMailSender mailSender;

    @Value("${frontend.reset-password-url}") private String resetUrl;

    public void sendResetEmail(String email) {
        userRepository.findByUsername(email).ifPresent(user -> {
            String code = UUID.randomUUID().toString();

            PasswordResetToken token = PasswordResetToken.builder()
                    .code(code)
                    .username(email)
                    .expiresAt(LocalDateTime.now().plusMinutes(30))
                    .used(false)
                    .build();
            tokenRepository.save(token);

            sendMail(email, code);
        });
    }

    public void resetPassword(String code, String newPassword) {
        PasswordResetToken token = tokenRepository.findByCode(code)
                .orElseThrow(() -> new AppException("Invalid reset code", 400));

        if (token.isUsed())
            throw new AppException("This reset code has already been used", 400);

        if (token.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new AppException("Reset code has expired", 400);

        User user = userRepository.findByUsername(token.getUsername())
                .orElseThrow(() -> new AppException("User not found", 404));

        keycloakService.resetKeycloakPassword(user.getKeycloakId(), newPassword);

        token.setUsed(true);
        tokenRepository.save(token);
    }

    private void sendMail(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Réinitialisation de votre mot de passe");
            message.setText(
                    "Bonjour,\n\n" +
                            "Vous avez demandé à réinitialiser votre mot de passe.\n" +
                            "Cliquez sur ce lien (valable 30 minutes) :\n\n" +
                            resetUrl + "/" + code + "\n\n" +
                            "Si vous n'êtes pas à l'origine de cette demande, ignorez ce message.\n\n" +
                            "L'équipe Scolaris"
            );
            mailSender.send(message);
        } catch (Exception e) {
            throw new AppException("Failed to send email: " + e.getMessage());
        }
    }
}