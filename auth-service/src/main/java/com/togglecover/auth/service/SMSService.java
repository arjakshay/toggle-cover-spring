package com.togglecover.auth.service;

public interface SMSService {
    boolean sendSMS(String phoneNumber, String message);
    boolean sendOTP(String phoneNumber, String otp, String templateId);
    boolean sendTransactionalSMS(String phoneNumber, String message, String templateId);
}