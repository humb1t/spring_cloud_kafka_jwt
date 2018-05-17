package org.nipu.poc.springcloudkafkajwtservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class SecurityCheckController {
    private final Logger log = LoggerFactory.getLogger(SecurityCheckController.class);

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @RequestMapping(method = RequestMethod.GET, path = "/secured/user")
    public ResponseEntity<String> checkUserRole() {
        return new ResponseEntity<>("Hello, User", HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.GET, path = "/secured/admin")
    public ResponseEntity<String> checkAdminRole() {
        return new ResponseEntity<>("Hello, Admin", HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.GET, path = "/unprotected")
    public ResponseEntity<String> doNotCheck() {
        return new ResponseEntity<>("Hello, be careful", HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/protected")
    public ResponseEntity<String> checkAuthorization() {
        return new ResponseEntity<>("Hello, Someone", HttpStatus.OK);
    }

    public String checkSecurityExpectations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Current authentication: {} ", authentication);
        OAuth2Authentication oAuth2Authentication = ((OAuth2Authentication) authentication);
        log.info("OAuth2 authentication: {} ", oAuth2Authentication);
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
        log.info("OAuth2 authentication details: {} ", details);
        return "Security check passed";
    }

}
