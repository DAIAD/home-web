package eu.daiad.scheduler.job.task;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.error.SchedulerErrorCode;
import eu.daiad.common.model.scheduling.Constants;
import eu.daiad.scheduler.job.builder.CommandExecutor;

/**
 * Task for executing a script using an external process.
 */
@Component
public class CommandLineTask extends BaseTask implements StoppableTasklet {

    private static final int DEFAULT_TIMEOUT = 60000;

    private static final Log logger = LogFactory.getLog(CommandLineTask.class);
    
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            String command = parameters.get(CommandLineTask.EnumInParameter.COMMAND.getValue());

            Integer timeout = DEFAULT_TIMEOUT;
            if(!StringUtils.isBlank(parameters.get(CommandLineTask.EnumInParameter.TIMEOUT.getValue()))) {
                timeout = Integer.parseInt(parameters.get(CommandLineTask.EnumInParameter.TIMEOUT.getValue()));
                if(timeout < DEFAULT_TIMEOUT) {
                    timeout = DEFAULT_TIMEOUT;
                }
            }

            String workDir = null;
            if(!StringUtils.isBlank(parameters.get(CommandLineTask.EnumInParameter.WORKING_DIRECTORY.getValue()))) {
                workDir = parameters.get(CommandLineTask.EnumInParameter.WORKING_DIRECTORY.getValue());

                File workDirFile = new File(workDir);
                if ((!workDirFile.exists()) || (!workDirFile.isDirectory())) {
                    workDir = null;
                }
            }

            String[] environmentVariables = null;
            if(!StringUtils.isBlank(CommandLineTask.EnumInParameter.ENVIRONMENT_VARS.getValue())) {
                environmentVariables = StringUtils.split(CommandLineTask.EnumInParameter.ENVIRONMENT_VARS.getValue(), " ");
                if(environmentVariables.length == 0) {
                    environmentVariables = null;
                }
            }


            CommandExecutor commandExecutor = new CommandExecutor(command,
                                                                  timeout,
                                                                  workDir,
                                                                  environmentVariables);

            if (commandExecutor.execute() == ExitStatus.COMPLETED) {
				String[] lines = commandExecutor.getOutput();
				for (final String line : lines) {
					logger.info(line);
				}
                String key = chunkContext.getStepContext().getStepName() +
                             Constants.PARAMETER_NAME_DELIMITER +
                             EnumOutParameter.OUTPUT.getValue();

                   chunkContext.getStepContext()
                               .getStepExecution()
                               .getExecutionContext()
                               .put(key, StringUtils.join(lines, System.getProperty("line.separator")));

            } else {
                throw createApplicationException(SchedulerErrorCode.SCHEDULER_JOB_STEP_FAILED)
                    .set("step", chunkContext.getStepContext().getStepName());
            }
        } catch(ApplicationException appEx) {
            throw appEx;
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
     * Enumeration of task input parameters.
     */
    public static enum EnumInParameter {
        /**
         * Command
         */
        COMMAND("command"),
        /**
         * Command execution timeout.
         */
        TIMEOUT("timeout"),
        /**
         * Working directory
         */
        WORKING_DIRECTORY("working.directory"),
        /**
         * Environment variables.
         */
        ENVIRONMENT_VARS("environment.vars");

        private final String value;

        public String getValue() {
            return value;
        }

        private EnumInParameter(String value) {
            this.value = value;
        }
    }

    /**
     * Enumeration of task output parameters.
     */
    public static enum EnumOutParameter {
        /**
         * Command output
         */
        OUTPUT("output");

        private final String value;

        public String getValue() {
            return value;
        }

        private EnumOutParameter(String value) {
            this.value = value;
        }
    }
}
