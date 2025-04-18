package com.biomedica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class UserDto {
    private String email;

    private String name;

    private String surname;
}
