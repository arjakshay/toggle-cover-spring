package com.togglecover.auth.service.impl;

import com.google.gson.JsonObject;
import com.togglecover.auth.config.Msg91Config;
import com.togglecover.auth.service.SMSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SMSServiceImpl implements SMSService {

    private final Msg91Config msg91Config;
    private final RestTemplate restTemplate;

    @Override
    public boolean sendSMS(String phoneNumber, String message) {
        // If demo mode is enabled, just log the message
        if (msg91Config.isDemoMode()) {
            log.info("DEMO MODE: SMS to {}: {}", phoneNumber, message);
            return true;
        }

        try {
            // Format phone number (remove +91 if present)
            String formattedPhone = phoneNumber.startsWith("+91") ?
                    phoneNumber.substring(3) : phoneNumber;

            // Build request body for MSG91
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("sender", msg91Config.getSenderId());
            requestBody.put("route", msg91Config.getRoute());
            requestBody.put("country", msg91Config.getCountry());

            Map<String, Object> sms = new HashMap<>();
            sms.put("message", message);
            sms.put("to", new String[]{formattedPhone});
            requestBody.put("sms", new Map[]{sms});

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("authkey", msg91Config.getAuthKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Send request
            String url = msg91Config.getBaseUrl() + "/v2/sendsms";
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            log.info("SMS sent to {}: Status {}", phoneNumber, response.getStatusCode());
            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
            // In demo mode, return true even if failed
            return msg91Config.isDemoMode();
        }
    }

    @Override
    public boolean sendOTP(String phoneNumber, String otp, String templateId) {
        String message = String.format("Your ToggleCover verification code is: %s. Valid for 5 minutes.", otp);
        return sendSMS(phoneNumber, message);
    }

    @Override
    public boolean sendTransactionalSMS(String phoneNumber, String message, String templateId) {
        return sendSMS(phoneNumber, message);
    }
}