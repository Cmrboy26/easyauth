package net.cmr.easyauth.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import net.cmr.easyauth.entity.EALogin;
import net.cmr.easyauth.resolver.EALoginResolver;
import net.cmr.easyauth.respository.EALoginRepository;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private EALoginRepository<? extends EALogin> loginRepository;

    @Override
    public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new EALoginResolver(loginRepository));
    }
}
