package eu.daiad.scheduler.job.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.common.model.error.SchedulerErrorCode;
import eu.daiad.common.model.query.savings.SavingScenario;
import eu.daiad.common.model.query.savings.SavingsConsumerSelectionFilter;
import eu.daiad.common.model.query.savings.TemporalSavingsConsumerSelectionFilter;
import eu.daiad.common.service.savings.ConsumerSelectionUtils;
import eu.daiad.scheduler.service.savings.ISavingsPotentialService;

/**
 * Task for exporting the members of all groups to a CSV file.
 */
@Component
public class SavingsPotentialExportMeterSerialTask extends BaseTask implements StoppableTasklet {

    /**
     * Delimiter character used for separating values in output file.
     */
    private static final char DELIMITER = ';';

    /**
     * Service for accessing savings potential scenario data.
     */
    @Autowired
    private ISavingsPotentialService savingsPotentialService;

    /**
     * Helper service for filtering users based on an instance of {@link SavingsConsumerSelectionFilter}.
     */
    @Autowired
    private ConsumerSelectionUtils consumerSelectionUtils;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        CSVPrinter printer = null;

        try {
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            // Get scenario
            if(StringUtils.isBlank(parameters.get(EnumInParameter.SCENARIO_KEY.getValue()))) {
                return RepeatStatus.FINISHED;
            }

            UUID scenarioKey = UUID.fromString(parameters.get(EnumInParameter.SCENARIO_KEY.getValue()));
            SavingScenario scenario = savingsPotentialService.find(scenarioKey);

            // Ensure that the working directory exists
            String workingDirectory = parameters.get(EnumInParameter.WORKING_DIRECTORY.getValue());
            ensureDirectory(new File(workingDirectory));

            // Ensure that the output filename parameters are set and construct the output filename.
            ensureParameter(parameters, EnumInParameter.OUTPUT_FILENAME.getValue());
            String filename = FilenameUtils.concat(workingDirectory, parameters.get(EnumInParameter.OUTPUT_FILENAME.getValue()));
            ensureFile(filename);

            CSVFormat format = CSVFormat.RFC4180.withDelimiter(DELIMITER);

            printer = new CSVPrinter(
                             new BufferedWriter(
                               new OutputStreamWriter(
                                 new FileOutputStream(filename, true),
                                 Charset.forName("UTF-8").newEncoder()
                               )
                             ),
                             format);

            export(printer, scenario.getParameters());
        } catch (Throwable t) {
            throw wrapApplicationException(t, SchedulerErrorCode.SCHEDULER_JOB_STEP_FAILED).set("step", chunkContext.getStepContext().getStepName());
        } finally {
            if (printer != null) {
                IOUtils.closeQuietly(printer);
            }
        }

        return RepeatStatus.FINISHED;
    }

    private void export(CSVPrinter printer, TemporalSavingsConsumerSelectionFilter parameters) throws IOException {
        for (String serial : consumerSelectionUtils.expandMeters(parameters)) {
            List<String> row = new ArrayList<String>();
            row.add(serial);
            printer.printRecord(row);
        }
    }

    @Override
    public void stop() {
        // TODO: Add business logic for stopping processing
    }

    /**
     * Enumeration of job input parameters.
     */
    public static enum EnumInParameter {
        /**
         * Savings potential scenario key.
         */
        SCENARIO_KEY("scenario.key"),
        /**
         * Working directory
         */
        WORKING_DIRECTORY("working.directory"),
        /**
         * Export filename
         */
        OUTPUT_FILENAME("output.filename");


        private final String value;

        public String getValue() {
            return value;
        }

        private EnumInParameter(String value) {
            this.value = value;
        }
    }
}
