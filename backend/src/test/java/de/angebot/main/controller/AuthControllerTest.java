package de.angebot.main.controller;

import de.angebot.main.security.payload.request.SignupRequest;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
class AuthControllerTest {

    @Autowired
    AuthController authController;

//    @Test
    void registerUser() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("arr");
        signupRequest.setPassword("777");
        signupRequest.setEmail("email@eamil.com");
        Set<String> set = new HashSet<>();
        set.add("admin");
        signupRequest.setRole(set);
        authController.registerUser(signupRequest);
    }
}
