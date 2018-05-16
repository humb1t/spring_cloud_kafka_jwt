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
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Objects;

@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;
    private final Sender sender;
    @Value("${spring.kafka.topic.messaging}")
    private String MESSAGING_TOPIC;

    @Autowired
    UserController(UserRepository userRepository, Sender sender) {
        this.userRepository = userRepository;
        this.sender = sender;
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @RequestMapping(method = RequestMethod.GET, path = "/members/{id}")
    public ResponseEntity<User> findByUserId(@PathVariable("id") Long id, Principal principal) {
        User result = userRepository.findOne(id);
        sentCredentials(principal);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private void sentCredentials(Principal principal) {
        log.info("Send Credentials of principal: {}", principal);
        if (Objects.isNull(principal)) {
            sender.send(MESSAGING_TOPIC, new MessageContainer("unauthorized"));
        }
        OAuth2AuthenticationDetails authenticationDetails = (OAuth2AuthenticationDetails) ((OAuth2Authentication) principal).getDetails();
        log.info("Authentication Details is {}", authenticationDetails);
        log.info("Token Value is {}", authenticationDetails.getTokenValue());
        Jwt jwt = JwtHelper.decode(authenticationDetails.getTokenValue());
        log.info("JWT is {}", jwt);
        sender.send(MESSAGING_TOPIC, new MessageContainer<>(String.valueOf(jwt)));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.GET, path = "/members")
    public ResponseEntity<Iterable<User>> getAll(Principal principal) {
        Iterable<User> all = userRepository.findAll();
        sentCredentials(principal);
        return new ResponseEntity<>(all, HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.POST, path = "/members")
    public ResponseEntity<User> register(@RequestBody User input, Principal principal) {
        User result = userRepository.save(input);
        sentCredentials(principal);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


}
