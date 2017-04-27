package eu.daiad.web.job.task;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.model.error.SchedulerErrorCode;
import eu.daiad.web.model.query.savings.Budget;
import eu.daiad.web.model.query.savings.BudgetSnapshot;
import eu.daiad.web.service.savings.IBudgetService;

/**
 * Task for computing active budget snapshots.
 */
@Component
public class BudgetProcessingTask extends BaseTask implements StoppableTasklet {

    /**
     * Default date format.
     */
    private static final String DEFAULT_DATE_FORMAT = "yyyyMMdd";

    /**
     * Service for managing budgets.
     */
    @Autowired
    private IBudgetService budgetService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            long jobId = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobId();
            DateTime referenceDate = resolveReferenceDate(parameters);

            for (Budget budget : budgetService.findActive()) {
                if (filterBudget(parameters, budget.getKey())) {
                    budgetService.createSnapshot(jobId, budget.getKey(), referenceDate.getYear(), referenceDate.getMonthOfYear());
                }
            }

            // Execute any pending snapshots
            for (BudgetSnapshot snapshot : budgetService.findPendingSnapshots()) {
                budgetService.createSnapshot(jobId, snapshot.getBudgetKey(), snapshot.getYear(), snapshot.getMonth());
            }
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

    /**
     * Filters a budget based on task parameter {@code BUDGET_KEY}.
     *
     * @param parameters the job parameters.
     * @param budgetKey the budget key to filter.
     * @return true if the budget must be updated.
     */
    private boolean filterBudget(Map<String, String> parameters, UUID budgetKey) {
        String selectedKey = parameters.get(EnumInParameter.BUDGET_KEY.getValue());
        if(StringUtils.isBlank(selectedKey)) {
            return true;
        }
        return UUID.fromString(selectedKey).equals(budgetKey);
    }

    /**
     * Resolves reference date
     *
     * @param parameters job parameters.
     * @return the reference date
     */
    private DateTime resolveReferenceDate(Map<String, String> parameters) {
        String dateFormat = parameters.get(EnumInParameter.DATE_FORMAT.getValue());
        if (StringUtils.isBlank(dateFormat)) {
            dateFormat = DEFAULT_DATE_FORMAT;
        }
        DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat);

        String date = parameters.get(EnumInParameter.REFERENCE_DATE.getValue());
        if (StringUtils.isBlank(date)) {
            return DateTime.now().minusMonths(1);
        }
        return formatter.parseDateTime(date);
    }

    /**
     * Enumeration of task input parameters.
     */
    public static enum EnumInParameter {
        /**
         * Optional date format parameter. Default value is yyyyMMdd.
         */
        DATE_FORMAT("date.format"),
        /**
         * Optional reference date. Default value is the current server date.
         */
        REFERENCE_DATE("reference.date"),
        /**
         * Optionally computing a snapshot for a specific budget only.
         */
        BUDGET_KEY("budget.key");

        private final String value;

        public String getValue() {
            return value;
        }

        private EnumInParameter(String value) {
            this.value = value;
        }
    }

}
