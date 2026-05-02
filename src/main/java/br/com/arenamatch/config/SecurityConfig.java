package br.com.arenamatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desabilita CSRF (necessário para o JSF/PrimeFaces funcionar sem configuração extra complexa)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configura as permissões de acesso
            .authorizeHttpRequests(auth -> auth
                // Como sua lógica de login é feita manualmente no LoginBean e na API,
                // vamos liberar o acesso a todas as requisições HTTP por enquanto.
                // Isso traz de volta o seu login.xhtml e permite o cadastro.html.
                .anyRequest().permitAll() 
            );

        return http.build();
    }
}