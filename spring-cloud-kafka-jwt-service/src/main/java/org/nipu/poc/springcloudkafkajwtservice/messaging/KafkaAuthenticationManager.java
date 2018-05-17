package org.nipu.poc.springcloudkafkajwtservice.messaging;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;

import java.util.Collection;

public class KafkaAuthenticationManager implements AuthenticationManager {
    private final String resourceId = null;
    private final DefaultTokenServices tokenServices;

    public KafkaAuthenticationManager(DefaultTokenServices tokenServices) {
        this.tokenServices = tokenServices;
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication == null) {
            throw new InvalidTokenException("Invalid token (token not found)");
        }
        String token = (String) authentication.getPrincipal();
        OAuth2Authentication auth = this.tokenServices.loadAuthentication(token);
        if (auth == null) {
            throw new InvalidTokenException("Invalid token: " + token);
        }
        Collection<String> resourceIds = auth.getOAuth2Request().getResourceIds();
        if (this.resourceId != null && resourceIds != null && !resourceIds.isEmpty() && !resourceIds.contains(this.resourceId)) {
            throw new OAuth2AccessDeniedException("Invalid token does not contain resource id (" + this.resourceId + ")");
        }
        this.checkClientDetails(auth);
        if (authentication.getDetails() instanceof OAuth2AuthenticationDetails) {
            OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
            // Guard against a cached copy of the same details
            if (!details.equals(auth.getDetails())) {
                // Preserve the authentication details from the one loaded by token services
                details.setDecodedDetails(auth.getDetails());
            }
        }
        auth.setDetails(authentication.getDetails());
        auth.setAuthenticated(true);
        return auth;
    }

    private void checkClientDetails(OAuth2Authentication auth) {

    }
}
