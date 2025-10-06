package net.cmr.easyauth.resolver;

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

@Component
public class EAAuthContextResolver<T extends EALogin> implements HandlerMethodArgumentResolver {
    
    private final EALoginRepository<T> loginRepository;

    @Autowired
    public EAAuthContextResolver(EALoginRepository<T> loginRepository) {
        this.loginRepository = loginRepository;
    }

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.hasParameterAnnotation(EasyAuth.class) && parameter.getParameterType().getSuperclass().equals(AuthContext.class);
    }

    @Override
    @Nullable
    public Object resolveArgument(@NonNull MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {
        AuthContext<T> context = new AuthContext<>();

        if (!(webRequest.getNativeRequest() instanceof HttpServletRequest)) {
            //LoginService.logger.info("Web request is NOT http servlet request");
            return null;
        }

        String accessJwt = JwtAuthenticationFilter.extractValidJwt((HttpServletRequest) webRequest.getNativeRequest(), true);
        String refreshJwt = JwtAuthenticationFilter.extractValidJwt((HttpServletRequest) webRequest.getNativeRequest(), false);

        if (accessJwt == null && refreshJwt == null) {
            return null;
        }
        
        // TODO: make auth context work

        context.setAccessJwt(accessJwt);
        context.setRefreshJwt(refreshJwt);

        return context;
    }

}
