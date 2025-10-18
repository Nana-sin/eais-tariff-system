package com.ttp.evaluation.recommendation.api.dto.classification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationRequestDto {

    @NotBlank(message = "Product name is required")
    @Size(max = 1000, message = "Product name must not exceed 1000 characters")
    private String productName;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String productDescription;

    @Pattern(regexp = "^\\d{6,10}$", message = "TN VED code must be 6-10 digits")
    private String tnVedCode;
}