package org.nipu.poc.springcloudkafkajwtservice;

import org.nipu.poc.springcloudkafkajwtservice.messaging.MessageContainer;
import org.nipu.poc.springcloudkafkajwtservice.messaging.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Objects;

@RestController
@RequestMapping(value = "/produce", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProducerController {

    private final Logger log = LoggerFactory.getLogger(ProducerController.class);
    private final Sender sender;
    @Value("${spring.kafka.topic.messaging}")
    private String MESSAGING_TOPIC;

    @Autowired
    ProducerController(Sender sender) {
        this.sender = sender;
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @RequestMapping(method = RequestMethod.GET, path = "/userMessage")
    public ResponseEntity<?> sendMessageWithUserCredentials(Principal principal) {
        sentCredentials(principal);
        return new ResponseEntity<>("Message with User JWT was sent to Kafka", HttpStatus.OK);
    }

    private void sentCredentials(Principal principal) {
        log.info("Send Credentials of principal: {}", principal);
        if (Objects.isNull(principal)) {
            sender.send(MESSAGING_TOPIC, new MessageContainer("unauthorized"));
        } else {
            OAuth2AuthenticationDetails authenticationDetails = (OAuth2AuthenticationDetails) ((OAuth2Authentication) principal).getDetails();
            log.info("Authentication Details is {}", authenticationDetails);
            final String tokenValue = authenticationDetails.getTokenValue();
            log.info("Token Value is {}", tokenValue);
            Jwt jwt = JwtHelper.decode(tokenValue);
            log.info("JWT is {}", jwt);
            sender.send(MESSAGING_TOPIC, new MessageContainer<>(String.valueOf(tokenValue)));
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.GET, path = "/adminMessage")
    public ResponseEntity<?> sendMessageWithAdminCredentials(Principal principal) {
        sentCredentials(principal);
        return new ResponseEntity<>("Message with Admin JWT was sent to Kafka", HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.GET, path = "/unauthorizedMessage")
    public ResponseEntity<?> sendMessageWithoutCredentials(Principal principal) {
        sentCredentials(principal);
        return new ResponseEntity<>("Message without JWT was sent to Kafka", HttpStatus.OK);
    }


}
