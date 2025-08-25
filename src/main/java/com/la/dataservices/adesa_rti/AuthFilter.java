package com.la.dataservices.adesa_rti;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
class AuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);
    private final AuthProps props;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        // secure only your webhook(s); everything else unchanged
        String p = req.getRequestURI();
        return !(p.startsWith("/events"));  // adjust as needed
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        // Only guard webhook endpoint(s)
        if (!req.getRequestURI().startsWith("/events")) {
            chain.doFilter(req, res);
            return;
        }

        String expected = props.getSecret();
        String provided = req.getHeader(props.getHeaderName());

        if (expected == null || expected.isBlank() || !expected.equals(provided)) {
            log.atInfo().setMessage("*** access denied ***").log();
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        chain.doFilter(req, res);
    }
}
