package intern.roima.hrmsbackend.services.Utils;

import java.util.List;

import jakarta.mail.MessagingException;

public interface MailService {

        void sendSimpleEmail(String to, String subject, String body);

        void sendSimpleEmail(List<String> toList, String subject, String body);

        void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException;

        void sendHtmlEmail(List<String> toList, String subject, String htmlBody) throws MessagingException;

        void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath)
                        throws MessagingException;

        void sendEmailWithAttachment(List<String> toList, String subject, String body, String attachmentPath)
                        throws MessagingException;

}