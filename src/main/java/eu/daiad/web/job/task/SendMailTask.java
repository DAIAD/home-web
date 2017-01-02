package eu.daiad.web.job.task;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.SurveyEntity;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SchedulerErrorCode;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IUtilityRepository;
import eu.daiad.web.service.mail.IMailService;
import eu.daiad.web.service.mail.MailTemplateModel;
import eu.daiad.web.service.mail.Message;

/**
 * Task that clusters users based on their consumption and computes water IQ status.
 */
@Component
public class SendMailTask extends BaseTask implements StoppableTasklet {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(SendMailTask.class);

    /**
     * Parameter name for the utilities identifiers. The value is a comma separated list of numbers.
     */
    private final String PARAMETER_UTILITY = "utility.id";

    /**
     * Parameter name for the email subject resource code.
     */
    private final String PARAMETER_SUBJECT = "mail.subject";

    /**
     * Parameter name for the email template.
     */
    private final String PARAMETER_TEMPLATE = "mail.template";

    /**
     * Service for sending mails
     */
    @Autowired
    private IMailService mailService;

    /**
     * Repository for accessing utility data.
     */
    @Autowired
    private IUtilityRepository utilityRepository;

    /**
     * Repository for accessing user data.
     */
    @Autowired
    private IUserRepository userRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();

            String[] utilities = StringUtils.split((String) jobParameters.get(PARAMETER_UTILITY), ',');
            String subject = (String) jobParameters.get(PARAMETER_SUBJECT);
            String template = (String) jobParameters.get(PARAMETER_TEMPLATE);

            for (String utilityIdAsString : utilities) {
                int totalMail = 0, failedMail = 0, sentMail = 0;
                int utilityId = Integer.parseInt(utilityIdAsString);

                UtilityInfo utility = utilityRepository.getUtilityById(utilityId);
                List<SurveyEntity> surveys = userRepository.getSurveyDataByUtilityId(utilityId);

                for (SurveyEntity s : surveys) {
                    try {
                        AccountEntity account = userRepository.getAccountByUsername(s.getUsername());

                        if ((account != null) && (account.getProfile().isSendMailEnabled())) {
                            MailTemplateModel model = new MailTemplateModel(account, account.getProfile());

                            Message message = new Message(template, model);

                            // Set locale
                            if (StringUtils.isBlank(account.getLocale())) {
                                // Skip user if locale is not set
                                continue;
                            }
                            message.setLocale(account.getLocale());

                            // Set subject
                            message.setSubject(messageSource.getMessage(subject, null, null, new Locale(account.getLocale())));
                            if (StringUtils.isBlank(message.getSubject())) {
                                // Skip user if message subject can not be resolved.
                                continue;
                            }

                            // Set recipient
                            if (StringUtils.isBlank(account.getFullname())) {
                                message.setRecipients(account.getUsername());
                            } else {
                                message.setRecipients(account.getUsername(), account.getFullname());
                            }

                            mailService.send(message);
                            sentMail++;
                        }
                    } catch (Exception ex) {
                        logger.info(String.format("Failed to send mail to account [%s]. Additional information: %s.",
                                                  s.getUsername(),
                                                  ex.getMessage()));
                        failedMail++;
                    }
                    totalMail++;
                }

                logger.info(String.format("Sending [%d] mails to utility [%s]. [%d] sent. [%d] failed.",
                                          totalMail,
                                          utility.getName(),
                                          sentMail,
                                          failedMail));
            }
        } catch (ApplicationException ex) {
            throw ex;
        } catch (Throwable t) {
            throw wrapApplicationException(t, SchedulerErrorCode.SCHEDULER_JOB_STEP_FAILED)
                    .set("step", chunkContext.getStepContext().getStepName());
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {
        // TODO: Add business logic for stopping processing
    }

}
