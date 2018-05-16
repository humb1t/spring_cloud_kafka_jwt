package org.nipu.poc.springcloudkafkajwtservice;

import org.nipu.poc.springcloudkafkajwtservice.messaging.MessageContainer;
import org.nipu.poc.springcloudkafkajwtservice.messaging.Sender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {


    @Value("${spring.kafka.topic.messaging}")
    private String MESSAGING_TOPIC;

    private final UserRepository userRepository;
    private final Sender sender;

    @Autowired
    UserController(UserRepository userRepository, Sender sender) {
        this.userRepository = userRepository;
        this.sender = sender;
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @RequestMapping(method = RequestMethod.GET, path = "/members/{id}")
    public ResponseEntity<User> findByUserId(@PathVariable("id") Long id, Principal principal) {
        User result = userRepository.findOne(id);
        sender.send(MESSAGING_TOPIC, new MessageContainer<>(String.valueOf(principal)));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.GET, path = "/members")
    public ResponseEntity<Iterable<User>> getAll(Principal principal) {
        Iterable<User> all = userRepository.findAll();
        sender.send(MESSAGING_TOPIC, new MessageContainer<>(String.valueOf(principal)));
        return new ResponseEntity<>(all, HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.POST, path = "/members")
    public ResponseEntity<User> register(@RequestBody User input, Principal principal) {
        User result = userRepository.save(input);
        sender.send(MESSAGING_TOPIC, new MessageContainer<>(String.valueOf(principal)));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


}
