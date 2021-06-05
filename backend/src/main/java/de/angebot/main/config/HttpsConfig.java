package de.angebot.main.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class HttpsConfig implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

//        String requestedPort = request.getHeader("X-Forwarded-Port"); // I'm behind a proxy on Heroku
         int requestedPort = request.getServerPort(); //if you're not behind a proxy

        if (requestedPort == 80) { // This will still allow requests on :8080
            response.sendRedirect("https://" + request.getServerName() + request.getRequestURI()
                    + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
            return false;
        }
        return true;
    }

}
