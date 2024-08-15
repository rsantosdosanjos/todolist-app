package br.com.resantosdosanjos.todolist.filters;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.resantosdosanjos.todolist.repositories.IUserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            var servletPath = request.getServletPath();
            if (servletPath.startsWith("/tasks")) {
                String[] credentials = extractCredentials(request);

                String username = credentials[0];
                String password = credentials[1];

                var user = userRepository.findByUsername(username);

                if (user == null || !isPasswordValid(password, user.getPassword())) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
                } else {
                    request.setAttribute("idUser", user.getId());
                    filterChain.doFilter(request, response);
                }
            } else {
                filterChain.doFilter(request, response);
            }
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Authorization header");
        }
    }

    private String[] extractCredentials(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }

        String authEncoded = authHeader.substring("Basic".length()).trim();
        String authDecoded = new String(Base64.getDecoder().decode(authEncoded));
        return authDecoded.split(":", 2);
    }

    private boolean isPasswordValid(String rawPassword, String encryptedPassword) {
        return BCrypt.verifyer().verify(rawPassword.toCharArray(), encryptedPassword).verified;
    }
}
