package eu.daiad.scheduler.job.task;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.profile.ComparisonRanking;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.repository.application.IUserRepository;
import eu.daiad.common.repository.application.IWaterIqRepository;

/**
 * Task for copying comparison and ranking data between two users.
 */
@Component
public class CopyUserComparisonDataTask extends BaseTask implements StoppableTasklet, InitializingBean {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(CopyUserComparisonDataTask.class);

    /**
     * Repository for accessing user data.
     */
    @Autowired
    private IUserRepository userRepository;

    /**
     * Repository for accessing water IQ data.
     */
    @Autowired
    @Qualifier("hBaseWaterIqRepository")
    private IWaterIqRepository hBaseWaterIqRepository;

    /**
     * Data source for executing SQL commands.
     */
    @Autowired
    private DataSource dataSource;

    /**
     * Spring JDBC template.
     */
    private JdbcTemplate jdbcTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

        // Get source properties
        UUID sourceUserKey = UUID.fromString(parameters.get(EnumInParameter.SOURCE_USER_KEY.getValue()));
        String sourceUserName = parameters.get(EnumInParameter.SOURCE_USER_NAME.getValue());

        // Get target properties
        UUID targetUserKey = UUID.fromString(parameters.get(EnumInParameter.TARGET_USER_KEY.getValue()));
        String targetUserName = parameters.get(EnumInParameter.TARGET_USER_NAME.getValue());

        if (sourceUserKey.equals(targetUserKey)) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The source and target user are the same.");
        }
        if (sourceUserName.equalsIgnoreCase(targetUserName)) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The source and target user names are the same.");
        }

        // Get users
        AuthenticatedUser sourceUser = userRepository.getUserByKey(sourceUserKey);
        if(sourceUser == null) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The source user was not found.");
        } else if (!sourceUser.getUsername().equals(sourceUserName)) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The source user key does not match the user name.");
        }
        AuthenticatedUser targetUser = userRepository.getUserByKey(targetUserKey);
        if(targetUser == null) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The target user was not found.");
        } else if (!targetUser.getUsername().equals(targetUserName)) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The target user key does not match the user name.");
        }

        // Transfer monthly data
        String command = String.format("delete from water_iq_history where account_id = %d", targetUser.getId());
        jdbcTemplate.execute(command);

        command = String.format(
          "INSERT INTO  water_iq_history(account_id, " +
          "                              created_on, " +
          "                              interval_from, " +
          "                              interval_to, " +
          "                              user_volume, " +
          "                              user_value, " +
          "                              similar_volume, " +
          "                              similar_value, " +
          "                              nearest_volume, " +
          "                              nearest_value,  " +
          "                              all_volume, " +
          "                              all_value, " +
          "                              user_1m_consumption, " +
          "                              similar_1m_consumption,  " +
          "                              nearest_1m_consumption, " +
          "                              all_1m_consumption, " +
          "                              interval_year, " +
          "                              interval_month) " +
          "SELECT       %d, " +
          "             created_on," +
          "             interval_from, " +
          "             interval_to, " +
          "             user_volume, " +
          "             user_value, " +
          "             similar_volume, " +
          "             similar_value, " +
          "             nearest_volume, " +
          "             nearest_value, " +
          "             all_volume,  " +
          "             all_value, user_1m_consumption, " +
          "             similar_1m_consumption, " +
          "             nearest_1m_consumption, " +
          "             all_1m_consumption, " +
          "             interval_year, " +
          "             interval_month " +
          "FROM         water_iq_history where account_id = %d;", targetUser.getId(), sourceUser.getId());
        jdbcTemplate.execute(command);

        // Transfer daily data
        List<ComparisonRanking.DailyConsumption> data = hBaseWaterIqRepository.getAllComparisonDailyConsumption(sourceUser.getKey());

        hBaseWaterIqRepository.storeDailyData(targetUserKey, data);

        StringBuilder text = new StringBuilder();
        text.append(String.format("Copied data from [%s] to [%s]. ", sourceUserName, targetUserName));
        text.append(String.format("Total points inserted    : %d\n", data.size()));
        logger.info(text.toString());

        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {
        // TODO: Add business logic for stopping processing
    }

    /**
     * Enumeration of task input parameters.
     */
    public static enum EnumInParameter {
        /**
         * Source user key.
         */
        SOURCE_USER_KEY("source.user.key"),
        /**
         * Source user name.
         */
        SOURCE_USER_NAME("source.user.name"),
        /**
         * Target user key.
         */
        TARGET_USER_KEY("target.user.key"),
        /**
         * Source user key.
         */
        TARGET_USER_NAME("target.user.name");

        private final String value;

        public String getValue() {
            return value;
        }

        private EnumInParameter(String value) {
            this.value = value;
        }
    }
}
