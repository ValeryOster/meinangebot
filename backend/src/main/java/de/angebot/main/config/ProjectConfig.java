package de.angebot.main.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public  class ProjectConfig {
    @Value("${activeProfile}")
    private static String activeProfile;

    public static String getActiveProfile() {
        return activeProfile;
    }
}
