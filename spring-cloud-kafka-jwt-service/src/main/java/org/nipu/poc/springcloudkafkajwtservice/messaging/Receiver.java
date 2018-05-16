package org.nipu.poc.springcloudkafkajwtservice.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CountDownLatch;

public class Receiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

    private final AuthenticationManager authenticationManager;
    private CountDownLatch latch = new CountDownLatch(1);
    private AuthenticationEventPublisher eventPublisher = new Receiver.NullEventPublisher();

    public Receiver(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @KafkaListener(topics = "${spring.kafka.topic.messaging}")
    public void receive(MessageContainer payload) {
        LOGGER.info("received payload='{}'", payload);
        final String tokenValue = (String) payload.getMessage();
        LOGGER.info("Token value {}", tokenValue);
        final Jwt jwt = JwtHelper.decode(tokenValue);
        LOGGER.info("Jwt decoded: {}", jwt);
        PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(
                tokenValue,
                ""
        );
        LOGGER.info("Authentication: {}", authentication);
        checkCurrentContext(authentication);
        if (authentication != null) {
            /*if (authentication instanceof AbstractAuthenticationToken) {
                AbstractAuthenticationToken needsDetails = (AbstractAuthenticationToken) authentication;
                LOGGER.info("Authentication token {} \n needs details", needsDetails);
            }*/
            Authentication authResult = authenticationManager.authenticate(authentication);
            LOGGER.info("Authentication success: {}", authResult);
            eventPublisher.publishAuthenticationSuccess(authResult);
            SecurityContextHolder.getContext().setAuthentication(authResult);
        }
        latch.countDown();
    }

    private void checkCurrentContext(PreAuthenticatedAuthenticationToken authentication) {
        if (authentication == null) {
            //TODO: clear SecurityContext as in C:/Users/Nikita_Puzankov/.m2/repository/org/springframework/security/oauth/spring-security-oauth2/2.0.14.RELEASE/spring-security-oauth2-2.0.14.RELEASE-sources.jar!/org/springframework/security/oauth2/provider/authentication/OAuth2AuthenticationProcessingFilter.java:135
            LOGGER.info("Should check and clean current context");
        }
    }

    private class NullEventPublisher implements AuthenticationEventPublisher {
        @Override
        public void publishAuthenticationSuccess(Authentication authentication) {
            LOGGER.info("publishAuthenticationSuccess: {}", authentication);
        }

        @Override
        public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
            LOGGER.info("publishAuthenticationSuccess: {} \n because of: {}", authentication, exception);
        }
    }
}
