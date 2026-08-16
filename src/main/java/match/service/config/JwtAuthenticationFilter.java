package match.service.config; // Check your package name

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import match.service.service.JwtService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@AllArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("MATCH SERVICE - Intercepted request to: {}", request.getRequestURI());

        String authHeader = request.getHeader("Authorization");
        log.info("MATCH SERVICE - Authorization Header: {}", authHeader);

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            log.warn("MATCH SERVICE - Missing or invalid header format. Skipping authentication.");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String email = jwtService.extractEmail(token);
            log.info("MATCH SERVICE - Extracted email: {}", email);

            if(email != null && SecurityContextHolder.getContext().getAuthentication() == null){
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                log.info("MATCH SERVICE - User loaded: {}", userDetails.getUsername());

                boolean isTokenValid = jwtService.validateToken(token, userDetails.getUsername());
                log.info("MATCH SERVICE - Is token valid?: {}", isTokenValid);

                if(isTokenValid){
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("MATCH SERVICE - Authentication successful!");
                } else {
                    log.warn("MATCH SERVICE - Token validation returned FALSE.");
                }
            }
        } catch (Exception e) {
            log.error("MATCH SERVICE - Exception during token processing: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request,response);
    }
}