package org.librairy.service.learner.service;

import com.google.common.base.Strings;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.librairy.service.learner.facade.model.TopicsRequest;
import org.librairy.service.modeler.facade.model.Settings;
import org.librairy.service.modeler.service.TopicsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class MailService {

    private static final Logger LOG = LoggerFactory.getLogger(MailService.class);

    @Value("#{environment['EMAIL_USER']?:'${email.user}'}")
    String emailUser;

    @Value("#{environment['EMAIL_PWD']?:'${email.pwd}'}")
    String emailPwd;

    private VelocityEngine velocityEngine;


    @PostConstruct
    public void setup() throws IOException {

        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();

        LOG.info("Mail Service initialized");
    }

    public void notifyError(TopicsRequest request, String message){

        try {
            LOG.info("Mail error notification of " + request + " because of " + message);
            Template t = velocityEngine.getTemplate("MailTemplateError.vm");

            VelocityContext context = new VelocityContext();

            StringWriter fw = new StringWriter();
            t.merge(context, fw);
            fw.close();
            mailTo(request.getContactEmail(), "["+ request.getName()+"] Something went wrong..", fw.toString() );
        } catch (IOException e) {
            LOG.error("Unexpected error sending an email",e);
        }

    }

    public void notifyCreation(TopicsRequest request, String message){

        try {
            LOG.info("Mail creation notification of " + request + " because of " + message);
            Template t = velocityEngine.getTemplate("MailTemplateSuccess.vm");

            VelocityContext context = new VelocityContext();
            String image = message;
            String signature = StringUtils.substringBefore(image,":");
            context.put("signature", signature);
            context.put("image", image);

            StringWriter fw = new StringWriter();
            t.merge(context, fw);
            fw.close();
            mailTo(request.getContactEmail(), "["+ request.getName()+"] Your Topic Model is ready!", fw.toString() );
        } catch (IOException e) {
            LOG.error("Unexpected error sending an email",e);
        }

    }


    private void mailTo(String dest, String subject, String txt){
        if (Strings.isNullOrEmpty(dest)) return;

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

            message.setSubject(subject);
            message.setText(txt);
            message.setSentDate(new Date());

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(dest,false));

            Transport.send(message);


            LOG.info("Mail sent!");

        } catch (Exception e) {
            LOG.warn("Mail error",e);
        }
    }


}
