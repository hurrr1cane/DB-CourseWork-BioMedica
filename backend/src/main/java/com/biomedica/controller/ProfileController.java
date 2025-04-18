package com.biomedica.controller;

import com.biomedica.dto.UpdateProfileRequest;
import com.biomedica.dto.UserDto;
import com.biomedica.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<UserDto> getProfile() {
        return ResponseEntity.ok(profileService.getProfile());
    }


    @PatchMapping
    public ResponseEntity<UserDto> updateProfile(@RequestBody @Valid UpdateProfileRequest updateProfileRequest) {
        return ResponseEntity.ok(profileService.updateProfile(updateProfileRequest));
    }
}
