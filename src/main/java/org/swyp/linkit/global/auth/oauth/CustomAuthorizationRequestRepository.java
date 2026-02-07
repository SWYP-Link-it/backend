package org.swyp.linkit.global.auth.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String RETURN_URL = "returnUrl";

    private final HttpSessionOAuth2AuthorizationRequestRepository delegate =
            new HttpSessionOAuth2AuthorizationRequestRepository();

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return delegate.loadAuthorizationRequest(request);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {

        if (authorizationRequest == null) {
            delegate.saveAuthorizationRequest(null, request, response);
            return;
        }

        String returnUrl = request.getParameter(RETURN_URL);
        if (returnUrl != null && !returnUrl.isBlank()) {
            Map<String, Object> additional =
                    new HashMap<>(authorizationRequest.getAdditionalParameters());
            additional.put(RETURN_URL, returnUrl);

            authorizationRequest = OAuth2AuthorizationRequest.from(authorizationRequest)
                    .additionalParameters(additional)
                    .build();
        }

        delegate.saveAuthorizationRequest(authorizationRequest, request, response);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        return delegate.removeAuthorizationRequest(request, response);
    }
}