package Patri.Stelmach.demo.Models;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

public class SecurityConfig
{
    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
