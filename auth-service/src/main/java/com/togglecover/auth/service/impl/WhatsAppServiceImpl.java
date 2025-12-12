package com.togglecover.auth.service.impl;

import com.togglecover.auth.config.TwilioConfig;
import com.togglecover.auth.service.WhatsAppService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppServiceImpl implements WhatsAppService {

    private final TwilioConfig twilioConfig;

    @PostConstruct
    public void init() {
        if (!twilioConfig.isDemoMode() && twilioConfig.isEnabled()) {
            try {
                Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
                log.info("Twilio initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize Twilio: {}", e.getMessage());
            }
        }
    }

    @Override
    public boolean sendMessage(String phoneNumber, String message) {
        // If demo mode is enabled, just log the message
        if (twilioConfig.isDemoMode()) {
            log.info("DEMO MODE: WhatsApp to {}: {}", phoneNumber, message);
            return true;
        }

        if (!twilioConfig.isEnabled()) {
            log.warn("WhatsApp service is disabled");
            return false;
        }

        try {
            // Format phone number for WhatsApp
            String formattedPhone = formatPhoneNumber(phoneNumber);
            String fromNumber = "whatsapp:" + twilioConfig.getWhatsappFrom();
            String toNumber = "whatsapp:" + formattedPhone;

            Message twilioMessage = Message.creator(
                    new PhoneNumber(toNumber),
                    new PhoneNumber(fromNumber),
                    message
            ).create();

            log.info("WhatsApp message sent to {}: SID {}", phoneNumber, twilioMessage.getSid());
            return true;

        } catch (Exception e) {
            log.error("Failed to send WhatsApp message to {}: {}", phoneNumber, e.getMessage());
            // In demo mode, return true even if failed
            return twilioConfig.isDemoMode();
        }
    }

    @Override
    public boolean sendOTP(String phoneNumber, String otp) {
        String message = String.format(
                "Your ToggleCover verification code is: *%s*\n\n" +
                        "This code will expire in 5 minutes.\n\n" +
                        "If you didn't request this code, please ignore this message.",
                otp
        );

        return sendMessage(phoneNumber, message);
    }

    @Override
    public boolean sendTemplateMessage(String phoneNumber, String templateName, Map<String, String> parameters) {
        String message = buildTemplateMessage(templateName, parameters);
        return sendMessage(phoneNumber, message);
    }

    private String buildTemplateMessage(String templateName, Map<String, String> parameters) {
        switch (templateName) {
            case "welcome":
                return String.format(
                        "Welcome to ToggleCover! 🎉\n\n" +
                                "Hi %s,\n\n" +
                                "Your account has been successfully created. " +
                                "You can now start earning insurance coverage while you work!\n\n" +
                                "Download our app: https://togglecover.app.link/download",
                        parameters.getOrDefault("name", "User")
                );
            case "otp":
                return String.format(
                        "Your verification code: *%s*\n\n" +
                                "Valid for 5 minutes.",
                        parameters.getOrDefault("otp", "123456")
                );
            default:
                return "Hello from ToggleCover!";
        }
    }

    private String formatPhoneNumber(String phoneNumber) {
        // Remove any non-digit characters
        String digits = phoneNumber.replaceAll("[^0-9]", "");

        // Add country code if not present
        if (!digits.startsWith("91") && digits.length() == 10) {
            return "+91" + digits;
        } else if (!digits.startsWith("+")) {
            return "+" + digits;
        }

        return digits;
    }
}