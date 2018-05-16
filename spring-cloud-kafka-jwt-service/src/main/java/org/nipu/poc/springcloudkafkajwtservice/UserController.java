package org.nipu.poc.springcloudkafkajwtservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private UserRepository userRepository;

    @Autowired
    UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @RequestMapping(method = RequestMethod.GET, path = "/members/{id}")
    public ResponseEntity<User> findByUserId(@PathVariable("id") Long id) {
        User result = userRepository.findOne(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.GET, path = "/members")
    public ResponseEntity<Iterable<User>> getAll() {
        Iterable<User> all = userRepository.findAll();
        return new ResponseEntity<>(all, HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.POST, path = "/members")
    public ResponseEntity<User> register(@RequestBody User input) {
        User result = userRepository.save(input);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


}
