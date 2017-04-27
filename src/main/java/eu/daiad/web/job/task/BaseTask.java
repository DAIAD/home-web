package eu.daiad.web.job.task;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.ErrorCode;
import eu.daiad.web.model.error.SchedulerErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;

/**
 * Base class for task implementations.
 */
public abstract class BaseTask {

    /**
     * Environment specific temporary directory.
     */
    private static final String PROPERTY_TMP_DIRECTORY = "java.io.tmpdir";

    /**
     * Delimiter for separating task name from the parameter name.
     */
    private static final String PARAMETER_NAME_DELIMITER = "::";

    /**
     * Spring application context.
     */
    @Autowired
    protected ApplicationContext applicationContext;

    /**
     * Resolves application messages and supports internationalization.
     */
    @Autowired
    protected MessageSource messageSource;

    /**
     * Creates a {@link ApplicationException} from the given {@link ErrorCode}.
     *
     * @param code the code.
     * @return the new exception.
     */
    protected ApplicationException createApplicationException(ErrorCode code) {
        String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

        return ApplicationException.create(code, pattern);
    }

    /**
     * Wraps a throwable with a new {@link ApplicationException}.
     *
     * @param t the throwable.
     * @return the new exception.
     */
    protected ApplicationException wrapApplicationException(Throwable t) {
        return this.wrapApplicationException(t, SharedErrorCode.UNKNOWN);
    }

    /**
     * Wraps a throwable with a new {@link ApplicationException} and assigns the given {@link ErrorCode}.
     *
     * @param t the throwable.
     * @param code the code.
     * @return the new exception.
     */
    protected ApplicationException wrapApplicationException(Throwable t, ErrorCode code) {
        String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

        return ApplicationException.wrap(t, code, pattern);
    }

    /**
     * Get job parameters specific to this step.
     *
     * @param stepContext step context.
     * @return a map of step parameters.
     */
    protected Map<String, String> getStepParameters(StepContext stepContext) {
        Map<String, String> parameters = new HashMap<String, String>();

        for (String qualifiedParameterName : stepContext.getJobParameters().keySet()) {
            String parameterName = getParameterName(stepContext.getStepName(), qualifiedParameterName);
            if (StringUtils.isBlank(parameterName)) {
                continue;
            }

            if (stepContext.getJobParameters().get(qualifiedParameterName) instanceof String) {
                parameters.put(parameterName, (String) stepContext.getJobParameters().get(qualifiedParameterName));
            }
        }

        mergeContextParameters(stepContext, parameters);

        return parameters;
    }


    /**
     * Merge parameters from the job execution context to the step parameters.
     *
     * @param stepContext the current step context.
     * @param parameters the existing parameters.
     */
    private void mergeContextParameters(StepContext stepContext, Map<String, String> parameters) {
        Map<String, Object> context = stepContext.getJobExecutionContext();

        for (String qualifiedParameterName : context.keySet()) {
            String parameterName = getParameterName(stepContext.getStepName(), qualifiedParameterName);
            if (StringUtils.isBlank(parameterName)) {
                continue;
            }

            if (context.get(qualifiedParameterName) instanceof String) {
                parameters.put(parameterName, (String) context.get(qualifiedParameterName));
            }
        }
    }

    /**
     * If a parameter belongs to this step, its name is returned; Otherwise null is returned.
     *
     * @param stepName the step name.
     * @param parameterName the parameter qualified name.
     * @return {@code true} if the parameter belongs to this step, its name is returned.
     */
    private String getParameterName(String stepName, String qualifiedParameterName) {
        String[] tokens = StringUtils.split(qualifiedParameterName, PARAMETER_NAME_DELIMITER);

        if (tokens.length != 2) {
            return null;
        }

        if (!tokens[0].equals(stepName)) {
            return null;
        }

        return tokens[1];
    }


    /**
     * Creates a temporary working directory.
     *
     * @param prefix working directory name prefix.
     * @return the new directory path.
     * @throws Exception if an I/O exception occurs.
     */
    protected String createWokringDirectory(String prefix) throws Exception {
        File tmpDir = new File(System.getProperty(PROPERTY_TMP_DIRECTORY));
        ensureDirectory(tmpDir);

        final File workDir;
        try {
            workDir = File.createTempFile(prefix, "", tmpDir);
        } catch (IOException ioe) {
            throw new Exception(String.format("Error creating temp dir in java.io.tmpdir %s due to %s.", tmpDir, ioe.getMessage()));
        }

        if (!workDir.delete()) {
            throw new Exception("Delete failed for " + workDir);
        }
        ensureDirectory(workDir);

        return workDir.getPath();
    }

    /**
     * Creates a directory.
     *
     * @param dir the directory to create.
     * @throws IOException if an I/O exception occurs.
     */
    protected void ensureDirectory(File dir) throws IOException {
        if (!dir.mkdirs() && !dir.isDirectory()) {
            throw new IOException("Failed to create " + dir.toString());
        }
    }

    /**
     * Ensures access to a file.
     *
     * @param filename the filename.
     * @throws IOException if an I/O error occurs.
     */
    protected void ensureFile(String filename) throws IOException {
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }

        file.createNewFile();
        file.delete();
    }

    /**
     * Checks if a parameter exists.
     *
     * @param parameters a map with all job parameters.
     * @param key the key of the parameter to check.
     * @throws ApplicationException if parameter does not exist.
     */
    protected void ensureParameter(Map<String, String> parameters, String key) throws ApplicationException {
        if(!parameters.containsKey(key)) {
            throw createApplicationException(SchedulerErrorCode.SCHEDULER_MISSING_PARAMETER).set("parameter", key);
        }
        if(StringUtils.isBlank(parameters.get(key))) {
            throw createApplicationException(SchedulerErrorCode.SCHEDULER_INVALID_PARAMETER).set("parameter", key)
                                                                                            .set("value", (String) parameters.get(key));
        }
    }

    /**
     * Finds a parameter and parses its value as an integer.
     *
     * @param parameters a map with all job parameters.
     * @param key the key of the parameter to check.
     * @return the value of the parameter as integer.
     *
     * @throws ApplicationException if either the parameter does not exist or the parameter value is not an integer.
     */
    protected int getInteger(Map<String, String> parameters, String key) throws ApplicationException {
        return this.getInteger(parameters, key, true);
    }

    /**
     * Finds a parameter and parses its value as an integer.
     *
     * @param parameters a map with all job parameters.
     * @param key the key of the parameter to check.
     * @param throwException if true and the value is not a valid integer expression, an exception is thrown.
     * @return the value of the parameter as integer.
     *
     * @throws ApplicationException if the {@code throwException} is {@code true} and either the
     *                              parameter does not exist or the parameter value is not an integer.
     */
    protected Integer getInteger(Map<String, String> parameters, String key, boolean throwException) throws ApplicationException {
        if((throwException) && (!parameters.containsKey(key))) {
            throw createApplicationException(SchedulerErrorCode.SCHEDULER_MISSING_PARAMETER).set("parameter", key);
        }
        if((throwException) && (StringUtils.isBlank(parameters.get(key)))) {
            throw createApplicationException(SchedulerErrorCode.SCHEDULER_INVALID_PARAMETER).set("parameter", key)
                                                                                            .set("value", (String) parameters.get(key));
        }
        try {
            return Integer.parseInt(parameters.get(key));
        } catch(NumberFormatException ex) {
            if(throwException) {
                throw createApplicationException(SchedulerErrorCode.SCHEDULER_INVALID_PARAMETER).set("parameter", key)
                                                                                                .set("value", (String) parameters.get(key));
            }
        }

        return null;
    }

}
