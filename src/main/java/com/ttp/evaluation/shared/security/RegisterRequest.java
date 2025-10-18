package com.ttp.evaluation.shared.security;

import com.ttp.evaluation.classification.domain.UserRole;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class RegisterRequest {
    @jakarta.validation.constraints.Email
    @jakarta.validation.constraints.NotBlank
    private String email;

    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Size(min = 8)
    private String password;

    @jakarta.validation.constraints.NotBlank
    private String fullName;

    @jakarta.validation.constraints.NotBlank
    private String company;

    @jakarta.validation.constraints.NotNull
    private UserRole role;
}
