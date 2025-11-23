package com.lara.sdc.config.security;

import com.lara.sdc.user.domain.model.User;
import com.lara.sdc.user.domain.model.AcessLevel;
import com.lara.sdc.user.domain.repository.IUserRepository;
import com.lara.sdc.doctor.domain.model.Doctor;
import com.lara.sdc.manager.domain.model.Manager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final IUserRepository userRepository;

    public CustomUserDetailsService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com email: " + email));

        List<SimpleGrantedAuthority> authorities = extractAuthorities(user);

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }

    private List<SimpleGrantedAuthority> extractAuthorities(User user) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if (user instanceof Doctor) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + AcessLevel.DOCTOR.level));
        } else if (user instanceof Manager) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + AcessLevel.MANAGER.level));
        }

        return authorities;
    }
}
