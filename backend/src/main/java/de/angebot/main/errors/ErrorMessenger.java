package de.angebot.main.errors;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ErrorMessenger {

    public void send(String message) {
        log.error(message);
    }
}
