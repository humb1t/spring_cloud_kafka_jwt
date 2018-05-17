package org.nipu.poc.springcloudkafkajwtservice;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class SecurityCheckController {

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


}
