package edu.nu.owaspapivulnlab.web.dto;

import edu.nu.owaspapivulnlab.model.AppUser;

public record UserDto(Long id, String username) {
    public static UserDto from(AppUser user) {
        return new UserDto(user.getId(), user.getUsername());
    }
}
