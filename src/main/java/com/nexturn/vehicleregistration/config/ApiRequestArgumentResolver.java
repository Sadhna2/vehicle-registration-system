package com.nexturn.vehicleregistration.config;

import com.nexturn.vehicleregistration.auth.ApiRequest;
import com.nexturn.vehicleregistration.dto.request.RequestInfo;
import com.nexturn.vehicleregistration.exception.AccessDeniedException;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class ApiRequestArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ApiRequest.class)
                && parameter.getParameterType() == RequestInfo.class;
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer container,
            NativeWebRequest request,
            WebDataBinderFactory binder) {

        var method = parameter.getMethod();
        boolean mutation = method != null
                && (method.isAnnotationPresent(PostMapping.class)
                || method.isAnnotationPresent(PutMapping.class)
                || method.isAnnotationPresent(PatchMapping.class)
                || method.isAnnotationPresent(DeleteMapping.class));

        if (mutation && !"XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            throw new AccessDeniedException("Required request header missing");
        }

        return new RequestInfo(
                request.getHeader("Authorization"),
                request.getHeader("Cookie"));
    }
}
