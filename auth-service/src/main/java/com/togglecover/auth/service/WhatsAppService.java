package com.togglecover.auth.service;

import java.util.Map;

public interface WhatsAppService {
    boolean sendMessage(String phoneNumber, String message);
    boolean sendOTP(String phoneNumber, String otp);
    boolean sendTemplateMessage(String phoneNumber, String templateName, Map<String, String> parameters);
}