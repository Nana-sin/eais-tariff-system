package com.ttp.evaluation.shared.security;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class AuthenticationResponse {
    private String token;
    private Long userId;
    private String email;
    private String role;
}
