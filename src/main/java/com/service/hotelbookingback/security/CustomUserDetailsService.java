package com.service.hotelbookingback.security;

import com.service.hotelbookingback.entities.User;
import com.service.hotelbookingback.exceptions.NotFoundException;
import com.service.hotelbookingback.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = null;
        try {
            user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new NotFoundException("User Not Found"));
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
        return AuthUser.builder()
                .user(user)
                .build();
    }
}
