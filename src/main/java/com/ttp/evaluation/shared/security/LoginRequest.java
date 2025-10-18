package com.ttp.evaluation.shared.security;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class LoginRequest {
    @jakarta.validation.constraints.Email
    @jakarta.validation.constraints.NotBlank
    private String email;

    @jakarta.validation.constraints.NotBlank
    private String password;
}
