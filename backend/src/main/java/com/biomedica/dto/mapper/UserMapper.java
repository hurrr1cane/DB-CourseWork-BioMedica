package com.biomedica.dto.mapper;

import com.biomedica.dto.UserDto;
import com.biomedica.entity.user.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        return UserDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .surname(user.getSurname())
                .build();
    }
}
