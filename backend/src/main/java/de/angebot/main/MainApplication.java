package de.angebot.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class MainApplication {

    public static void main(String[] args) {
        System.out.println("Hier vor start");
         SpringApplication.run(MainApplication.class, args);
        System.out.println("Hier danach");
    }

}
