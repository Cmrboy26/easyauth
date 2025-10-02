package net.cmr.easyauth.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import net.cmr.easyauth.controller.AuthContextualizerResolver;
import net.cmr.easyauth.repository.LoginRepository;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired private LoginRepository loginRepository;

    @Override
    public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthContextualizerResolver(loginRepository));
    }

}
