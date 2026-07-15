package com.blogforge.security;

import com.blogforge.entity.User;
import com.blogforge.exception.MessageResolver;
import com.blogforge.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final MessageResolver messageResolver;

    public CustomUserDetailsService(UserRepository userRepository, MessageResolver messageResolver) {
        this.userRepository = userRepository;
        this.messageResolver = messageResolver;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        messageResolver.getMessage("authentication.username.not-found", username)
                ));

        return new CustomUserDetails(u);
    }
}
