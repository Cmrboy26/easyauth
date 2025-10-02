package net.cmr.easyauth.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;
import net.cmr.easyauth.entity.Login;
import net.cmr.easyauth.repository.LoginRepository;
import net.cmr.easyauth.security.JwtAuthenticationFilter;
import net.cmr.easyauth.service.LoginService;

@Component
public class AuthContextualizerResolver implements HandlerMethodArgumentResolver {

    private final LoginRepository loginRepository;

    @Autowired
    public AuthContextualizerResolver(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthContextualize.class) && parameter.getParameterType().equals(AuthContext.class);
    }

    @Override
    @Nullable
    public Object resolveArgument(@NonNull MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {
        AuthContext context = new AuthContext();

        if (!(webRequest.getNativeRequest() instanceof HttpServletRequest)) {
            LoginService.logger.info("Web request is NOT http servlet request");
            return null;
        }

        String jwt = JwtAuthenticationFilter.getJwtToken((HttpServletRequest) webRequest.getNativeRequest());
        if (jwt == null) {
            return null;
        }
        String idString = LoginService.getIdFromJwt(jwt);
        Long id = null;
        try {
            id = Long.parseLong(idString);
        } catch (NumberFormatException e) {
            return null;
        }

        Optional<Login> login = loginRepository.findById(id);
        if (login.isEmpty()) {
            return context;
        }
        context.setJwt(jwt);
        context.setLogin(login.get());

        return context;
    }

}
