package net.cmr.easyauth.resolver;

import java.util.Optional;
import java.util.Scanner;

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
import net.cmr.easyauth.entity.EALogin;
import net.cmr.easyauth.filter.JwtAuthenticationFilter;
import net.cmr.easyauth.respository.EALoginRepository;
import net.cmr.easyauth.util.JwtUtil;

@Component
public class EALoginResolver implements HandlerMethodArgumentResolver {
    
    private final EALoginRepository<? extends EALogin> loginRepository;

    @Autowired
    public EALoginResolver(EALoginRepository<? extends EALogin> loginRepository) {
        this.loginRepository = loginRepository;
    }

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.hasParameterAnnotation(EasyAuth.class) 
            && (parameter.getParameterType().equals(EALogin.class) || parameter.getParameterType().getSuperclass().equals(EALogin.class));
    }

    @Override
    @Nullable
    public Object resolveArgument(@NonNull MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {
        if (!(webRequest.getNativeRequest() instanceof HttpServletRequest)) {
            //LoginService.logger.info("Web request is NOT http servlet request");
            System.out.println("Web request is NOT HTTP Servlet Request");
            return null;
        }

        String accessJwt = JwtAuthenticationFilter.extractValidJwt((HttpServletRequest) webRequest.getNativeRequest(), true);
        String refreshJwt = JwtAuthenticationFilter.extractValidJwt((HttpServletRequest) webRequest.getNativeRequest(), false);

        if (accessJwt == null && refreshJwt == null) {
            return null;
        }
        Long id = null;
        if (refreshJwt != null) {
            id = JwtUtil.getId(refreshJwt);
        }
        if (accessJwt != null) {
            Long accessId = JwtUtil.getId(accessJwt);
            if (id != null && accessId != id) {
                // Both IDS are present, but they're different
                throw new IllegalArgumentException("Passed access and request JWT yield different IDs");
            }
            id = accessId;
        }
        if (id == null) {
            throw new NullPointerException();
        }

        Optional<? extends EALogin> login = loginRepository.findById(id);
        if (login.isEmpty()) {
            return null;
        }

        return login.get();
    }

}
