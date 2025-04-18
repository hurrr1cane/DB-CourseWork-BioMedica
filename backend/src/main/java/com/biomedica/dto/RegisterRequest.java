package com.biomedica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotNull
    @Email
    private String email;

    @NotNull
    @Length(min = 6, max = 50)
    private String password;

    @Length(min = 3, max = 50)
    private String name;

    @Length(min = 3, max = 50)
    private String surname;

}
