// AlertService.java
package com.la.dataservices.adesa_rti;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AlertService {
    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String to;
    private final String from;

    private volatile Instant windowStart = Instant.now();
    private final AtomicInteger sentInWindow = new AtomicInteger(0);
    private static final int MAX_PER_MINUTE = 10;

    public AlertService(JavaMailSender mailSender,
                        @Value("${alerts.enabled:true}") boolean enabled,
                        @Value("${alerts.to}") String to,
                        @Value("${alerts.from}") String from) {
        this.mailSender = mailSender; this.enabled = enabled; this.to = to; this.from = from;
    }

    @Async  // don’t block request thread
    public void sendError(String subject, String body) {
        if (!enabled) return;

        // simple 60s rate limit
        var now = Instant.now();
        if (now.minusSeconds(60).isAfter(windowStart)) { windowStart = now; sentInWindow.set(0); }
        if (sentInWindow.incrementAndGet() > MAX_PER_MINUTE) return;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from); msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        mailSender.send(msg);
    }
}
