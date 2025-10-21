package com.barrisense.backend.auth.service.mappers;

import com.barrisense.backend.auth.domain.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class Mappers {
    public UserDetails mapUserToUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.name()))
                        .toList())
                .build();
    }
}
