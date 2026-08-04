package com.employeetracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.admin.email}")
    private String adminEmail;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private void sendMail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Mail send failed: " + e.getMessage());
        }
    }

    public void sendRegistrationEmail(String to, String fullName, String username,
                                       String rawPassword, Long employeeId) {
        String subject = "Welcome to Employee Tracker - Your Account Details";
        String body = "Hi " + fullName + ",\n\n"
                + "Your account has been created successfully.\n\n"
                + "Employee ID: " + employeeId + "\n"
                + "Username: " + username + "\n"
                + "Password: " + rawPassword + "\n\n"
                + "Please keep this information safe.\n\n"
                + "Regards,\nEmployee Tracker Team";
        sendMail(to, subject, body);
    }

    public void sendLoginEmail(String to, String fullName) {
        String subject = "Successful Login Alert";
        String body = "Hi " + fullName + ",\n\n"
                + "You have successfully logged in to Employee Tracker at "
                + LocalDateTime.now() + ".\n\n"
                + "If this wasn't you, please contact your admin immediately.\n\n"
                + "Regards,\nEmployee Tracker Team";
        sendMail(to, subject, body);
    }

    public void sendLogoutEmail(String to, String fullName, LocalDateTime logoutTime, String duration) {
        String subject = "Logged Out Successfully";
        String body = "Hi " + fullName + ",\n\n"
                + "You have been logged out.\n\n"
                + "Logout Time: " + logoutTime + "\n"
                + "Session Duration: " + duration + "\n\n"
                + "Regards,\nEmployee Tracker Team";
        sendMail(to, subject, body);
    }

    public void sendAdminNewEmployeeNotification(String employeeName, Long employeeId, String employeeEmail) {
        String subject = "New Employee Registered";
        String body = "A new employee has registered:\n\n"
                + "Employee Name: " + employeeName + "\n"
                + "Employee ID: " + employeeId + "\n"
                + "Email: " + employeeEmail + "\n\n"
                + "Regards,\nEmployee Tracker System";
        sendMail(adminEmail, subject, body);
    }

    public void sendOtpEmail(String to, String fullName, String otp) {
        String subject = "Employee Location Tracker - Password Reset OTP";
        String body = "Hello " + fullName + ",\n\n"
                + "Your OTP for password reset is:\n\n"
                + otp + "\n\n"
                + "This OTP is valid for 5 minutes.\n\n"
                + "Do not share this OTP with anyone.\n\n"
                + "Regards,\nEmployee Location Tracker Team";
        sendMail(to, subject, body);
    }
    public void sendPasswordChangedEmail(String to, String fullName) {
        String subject = "Password Changed Successfully";
        String body = "Hello " + fullName + ",\n\n"
                + "Your password has been changed successfully.\n\n"
                + "If this was not you, please contact the administrator immediately.\n\n"
                + "Regards,\nEmployee Location Tracker Team";
        sendMail(to, subject, body);
    }
}
