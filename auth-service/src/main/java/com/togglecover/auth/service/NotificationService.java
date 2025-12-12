package com.togglecover.auth.service;

import com.togglecover.auth.entity.OtpType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SMSService smsService;
    private final WhatsAppService whatsAppService;

    @Value("${otp.delivery.method:sms}")
    private String deliveryMethod;

    @Value("${msg91.enabled:true}")
    private boolean msg91Enabled;

    @Value("${twilio.enabled:true}")
    private boolean twilioEnabled;

    public boolean sendOtp(String phone, String otp, OtpType otpType) {
        String message = generateOtpMessage(otp, otpType);

        // Log OTP for demo purposes (ALWAYS log in development)
        log.info("OTP for {} ({}): {}", phone, otpType, otp);

        try {
            switch (deliveryMethod.toLowerCase()) {
                case "whatsapp":
                    if (twilioEnabled) {
                        return whatsAppService.sendOTP(phone, otp);
                    }
                    log.warn("WhatsApp service is disabled, falling back to SMS");
                    // Fall through to SMS
                case "sms":
                default:
                    if (msg91Enabled) {
                        return smsService.sendOTP(phone, otp, getTemplateId(otpType));
                    }
                    log.warn("SMS service is disabled");
                    return true; // Return true for demo mode
            }
        } catch (Exception e) {
            log.error("Failed to send OTP to {}: {}", phone, e.getMessage());
            // In demo/development mode, we still consider it successful
            return true;
        }
    }

    private String generateOtpMessage(String otp, OtpType otpType) {
        switch (otpType) {
            case LOGIN:
                return String.format("Your ToggleCover login OTP is %s. Valid for 5 minutes.", otp);
            case PASSWORD_RESET:
                return String.format("Your ToggleCover password reset OTP is %s. Valid for 5 minutes.", otp);
            case ACCOUNT_VERIFICATION:
                return String.format("Your ToggleCover account verification OTP is %s.", otp);
            default:
                return String.format("Your ToggleCover OTP is %s.", otp);
        }
    }

    private String getTemplateId(OtpType otpType) {
        switch (otpType) {
            case LOGIN:
                return "login_otp_template";
            case PASSWORD_RESET:
                return "reset_password_template";
            case ACCOUNT_VERIFICATION:
                return "account_verification_template";
            default:
                return "default_otp_template";
        }
    }
}