package intern.roima.hrmsbackend.services.Message_Module;

import java.io.File;
import java.util.List;

import intern.roima.hrmsbackend.dtos.Responses.EmailDto;

public interface EmailService {

    EmailDto sendEmail(String templateName, List<String> recipients, List<File> attachments);

    EmailDto sendTravelAssignmentEmail(String templateName, List<String> recipients, Long traveId);

    EmailDto sendExpenseEmail(String templateName, String hrEmail, Long expenseId);

    EmailDto sendWarningEmail(String templateName, String recipientEmail, String reason);

    EmailDto sendJobWithAttachment(List<String> recipients, Long jobId);

    EmailDto sendReferralWithCV(List<String> reviewers, Long referralId);
}
