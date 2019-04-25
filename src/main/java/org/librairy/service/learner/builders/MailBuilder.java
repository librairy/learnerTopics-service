package org.librairy.service.learner.builders;

import com.google.common.base.Strings;
import es.upm.oeg.librairy.service.modeler.facade.model.Settings;
import es.upm.oeg.librairy.service.modeler.service.TopicsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;


/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

@Component
public class MailBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(MailBuilder.class);

    @Value("#{environment['EMAIL_USER']?:'${email.user}'}")
    String emailUser;

    @Value("#{environment['EMAIL_PWD']?:'${email.pwd}'}")
    String emailPwd;


    public void newMailTo(String dest, TopicsService topicsService){
        if (Strings.isNullOrEmpty(dest)) return;

        LOG.info("sending an email with report .. ");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailUser, emailPwd);
            }
        };

        Session session = Session.getInstance(props, auth);

        try {

            Message message = new MimeMessage(session);
            message.addHeader("Content-type", "text/HTML; charset=UTF-8");
            message.addHeader("format", "flowed");
            message.addHeader("Content-Transfer-Encoding", "8bit");
            message.setFrom(new InternetAddress("no_reply@librairy.org","librAIry"));
            message.setReplyTo(InternetAddress.parse("no_reply@librairy.org", false));

            message.setSubject("Your Topic Model is ready!");

            StringBuilder description = new StringBuilder();
            description.append("# Topics \n");
            topicsService.getTopics().forEach(topic -> {
                description.append("- '"+topic.getId()+"': {"+topic.getDescription()+"}\n");
            });

            Settings model = topicsService.getSettings();
            description.append("\n# Params \n");
            description.append("- algorithm: ").append(model.getAlgorithm()).append("\n");
            model.getParams().entrySet().stream().forEach(entry -> {
                description.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            });

            description.append("\n# Stats \n");
            model.getStats().entrySet().stream().forEach(entry -> {
                description.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            });

            message.setText(description.toString());
            message.setSentDate(new Date());

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(dest,false));

            Transport.send(message);


            LOG.info("Mail sent!");

        } catch (Exception e) {
            LOG.warn("Mail error",e);
        }
    }

}
