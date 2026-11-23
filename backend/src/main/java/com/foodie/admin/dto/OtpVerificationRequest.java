package com.foodie.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OtpVerificationRequest(
        @NotBlank @Pattern(regexp = "\\d{6}", message = "OTP must be six digits") String otp) {
}
