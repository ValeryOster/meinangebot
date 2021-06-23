package de.angebot.main;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomcat.util.net.TLSClientHelloExtractor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;

import java.nio.charset.StandardCharsets;


@SpringBootApplication
@EnableConfigurationProperties
public class MainApplication {

    public static void main(String[] args) {
        setRedirectionHttpToHttps();
        SpringApplication.run(MainApplication.class, args);
    }

    @Bean
    public ServletWebServerFactory servletContainer() {
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
        tomcat.addAdditionalTomcatConnectors(redirectConnector());
        return tomcat;
    }

    private Connector redirectConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("https");
        connector.setPort(80);
        connector.setSecure(false);
        connector.setRedirectPort(443);
        return connector;
    }

    private static void setRedirectionHttpToHttps() {
        TLSClientHelloExtractor.USE_TLS_RESPONSE = ("<html>\n"+
                "<head>\n" +
                "<title> Redicting to HTTPS</title> <meta http-equiv='refresh' content=\"0; " +
                "url=https://localhost:443/\" />" +
                "</head>\n" +
                "<script Language=JavaScript>\n" +
                "Function redirectHttpToHttps()\n" +
                "{" +
                "window.location = 'https://localhost:443/';\n" +
                "}" +
                "redirectHttpToHttps();" +
                "</script>" +
                "</body>" +
                "</html> ").getBytes(StandardCharsets.UTF_8);
    }
}
