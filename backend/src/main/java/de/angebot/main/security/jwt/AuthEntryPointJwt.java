package de.angebot.main.security.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Slf4j
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
						 AuthenticationException authException) throws IOException, ServletException {
		log.error("Unauthorized error: {}", authException.getMessage());
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getWriter(), Map.of(
				"status", HttpServletResponse.SC_UNAUTHORIZED,
				"error", "Unauthorized",
				"message", "Error: " + authException.getMessage(),
				"path", request.getRequestURI()
		));
	}

}
