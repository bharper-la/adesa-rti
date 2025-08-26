package com.la.dataservices.adesa_rti;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {

    private final AuthProps props;
    private static final AntPathMatcher PATHS = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Only protect webhook endpoints
        return !PATHS.match("/events/**", request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        final String apiKeyHeaderName   = props.getApiKeyHeaderName();
        final String secretHeaderName   = props.getHeaderName();
        final Map<String, String> map   = props.getClients();

        final String apiKey        = req.getHeader(apiKeyHeaderName);
        final String providedSecret= req.getHeader(secretHeaderName);

        // Must have a key
        if (apiKey == null) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            return;
        }

        // Must be a known key
        final String expectedSecret = map != null ? map.get(apiKey) : null;
        if (expectedSecret == null) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            return;
        }

        // Secret must match for that key (constant-time compare)
        if (!equalsConstTime(providedSecret, expectedSecret)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            return;
        }

        var auth = new UsernamePasswordAuthenticationToken(
                apiKey, null, List.of(new SimpleGrantedAuthority("ROLE_WEBHOOK")));
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(req, res);
    }

    private boolean equalsConstTime(String a, String b) {
        if (a == null || b == null) return false;
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
