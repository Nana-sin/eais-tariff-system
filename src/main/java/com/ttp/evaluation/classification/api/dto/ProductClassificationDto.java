package com.ttp.evaluation.classification.api.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductClassificationDto {

    private Long id;

    @NotBlank(message = "TN VED code is required")
    @Pattern(regexp = "^\\d{2,10}$", message = "TN VED code must be 2-10 digits")
    private String tnVedCode;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private String parentCode;

    @NotNull(message = "Level is required")
    @Min(value = 2, message = "Level must be at least 2")
    @Max(value = 10, message = "Level must not exceed 10")
    private Integer level;

    private Boolean active;
}