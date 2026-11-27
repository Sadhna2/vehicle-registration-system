package com.nexturn.vehicleregistration.security;

import com.nexturn.vehicleregistration.exception.InvalidTokenException;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/** Lets controller methods declare an {@link Actor} parameter for the authenticated caller. */
public class CurrentActorResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType() == Actor.class;
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer container,
      NativeWebRequest request,
      WebDataBinderFactory binder) {
    Object actor = request.getAttribute(CurrentActor.ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
    if (actor == null) {
      throw new InvalidTokenException("Sign in to continue.");
    }
    return actor;
  }
}
