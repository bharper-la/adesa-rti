package com.la.dataservices.adesa_rti;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
public class TestNotifications {

    @Autowired
    JavaMailSender mailSender;

    @Test
    public void testEmail() {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("tst_server@wavisys.net");
        msg.setTo("bradharper@laserappraiser.com");
        msg.setSubject("Test SES Email");
        msg.setText("If you see this, SES SMTP is working.");
        //mailSender.send(msg);
    }


}
