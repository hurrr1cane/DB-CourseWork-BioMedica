package com.biomedica.dto;

import com.biomedica.dto.validation.PatchValidation;
import com.biomedica.dto.validation.PostValidation;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;
import java.util.UUID;

@Data
public class LaboratoryRequest {
    @NotNull(groups = PostValidation.class)
    @Length(min = 2, max = 255, groups = {PostValidation.class, PatchValidation.class})
    private String address;

    @NotNull(groups = PostValidation.class)
    private String workingHours;

    @NotNull(groups = PostValidation.class)
    private String phoneNumber;

    private List<UUID> testIds;
}
