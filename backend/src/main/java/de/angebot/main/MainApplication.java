package de.angebot.main;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.servlet.ServletWebServerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    /**
     * Bei {@code server.http-redirect.enabled=true} lauscht zusätzlich ein HTTP-Connector
     * (Port {@code server.http-redirect.port}, Standard 80) und leitet alle Anfragen auf HTTPS
     * ({@code server.port}, z. B. 443) um.
     */
    @Bean
    public ServletWebServerFactory servletContainer(
            @Value("${server.http-redirect.enabled:true}") boolean httpRedirectEnabled,
            @Value("${server.http-redirect.port:80}") int httpPort,
            @Value("${server.port:443}") int httpsPort) {
        if (!httpRedirectEnabled) {
            return new TomcatServletWebServerFactory();
        }
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        tomcat.addAdditionalConnectors(redirectConnector(httpPort, httpsPort));
        return tomcat;
    }

    private Connector redirectConnector(int httpPort, int httpsPort) {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(httpPort);
        connector.setSecure(false);
        connector.setRedirectPort(httpsPort);
        return connector;
    }
}
