package it.unipi.nexusscholar.security;

import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtTokenFilterTest {

  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain filterChain;

  @InjectMocks private JwtTokenFilter jwtTokenFilter;

  @Test
  void doFilterInternal_ValidToken_SetsAuthentication() throws Exception {
    String token = "valid-token";
    UsernamePasswordAuthenticationToken auth = mock(UsernamePasswordAuthenticationToken.class);

    when(jwtTokenProvider.resolveToken(request)).thenReturn(token);
    when(jwtTokenProvider.validateToken(token)).thenReturn(true);
    when(jwtTokenProvider.getAuthentication(token)).thenReturn(auth);

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(jwtTokenProvider).getAuthentication(token);
    verify(filterChain).doFilter(request, response);

    // Cleanup context
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilterInternal_InvalidToken_DoesNotSetAuthentication() throws Exception {
    String token = "invalid-token";

    when(jwtTokenProvider.resolveToken(request)).thenReturn(token);
    when(jwtTokenProvider.validateToken(token)).thenReturn(false);

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(jwtTokenProvider, never()).getAuthentication(anyString());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_NoToken_ContinuesChain() throws Exception {
    when(jwtTokenProvider.resolveToken(request)).thenReturn(null);

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(jwtTokenProvider, never()).validateToken(anyString());
    verify(filterChain).doFilter(request, response);
  }
}
