package br.com.arenamatch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Mapeia a raiz "/" para redirecionar para "/login.xhtml"
        registry.addViewController("/")
                .setViewName("redirect:/login.xhtml");
        
        // Define alta prioridade para garantir que essa regra vença outras padrões
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }
}