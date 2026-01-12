package it.unipi.nexusscholar.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor // Uses Lombok for clean constructor injection
public class JwtTokenFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    // Use the injected instance 'jwtTokenProvider' instead of the static class
    String token = jwtTokenProvider.resolveToken(request);

    // 1. Check if the token exists and is valid
    if (token != null && jwtTokenProvider.validateToken(token)) {

      // 2. Extract user identity and permissions
      UsernamePasswordAuthenticationToken auth = jwtTokenProvider.getAuthentication(token);

      // 3. Set the authentication in the Spring Security Context
      if (auth != null) {
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    }

    // Continue the filter chain
    filterChain.doFilter(request, response);
  }
}
