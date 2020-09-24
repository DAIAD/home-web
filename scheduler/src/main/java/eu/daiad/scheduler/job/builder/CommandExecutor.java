package eu.daiad.scheduler.job.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.step.tasklet.SimpleSystemProcessExitCodeMapper;
import org.springframework.batch.core.step.tasklet.SystemCommandException;
import org.springframework.batch.core.step.tasklet.SystemProcessExitCodeMapper;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

/**
 * Utility class for executing shell commands.
 */
public class CommandExecutor {

    /**
     * The command to execute.
     */
    private String command;

    /**
     * Environment parameters to set during the command execution.
     */
    private String[] environmentParams = {};

    /**
     * Working directory of the process that executes the shell command.
     */
    private File workingDirectory = null;

    /**
     * Maps the exit code of a system process to {@link ExitStatus} value.
     */
    private SystemProcessExitCodeMapper systemProcessExitCodeMapper = new SimpleSystemProcessExitCodeMapper();

    /**
     * Command execution timeout in milliseconds.
     */
    private long timeout = 0;

    /**
     * Timeout polling interval in milliseconds.
     */
    private long checkInterval = 2000;

    /**
     * Simple {@link TaskExecutor} for executing threads.
     */
    private TaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();

    /**
     * True if the thread executing the task should be interrupted when timeout
     * expires.
     */
    private boolean interruptOnCancel = false;

    /**
     * The process that executes the command.
     */
    private Process process;

    /**
     * Stores shell output. The caller may parse the output to decide if script
     * has been executed successfully.
     */
    private List<String> output = new ArrayList<String>();

    /**
     * Creates a new {@link CommandExecutor}.
     *
     * @param command the command to execute.
     * @param timeout execution timeout in milliseconds.
     */
    public CommandExecutor(String command, long timeout) {
        this.command = command;
        this.timeout = timeout;
    }

    /**
     * Creates a new {@link CommandExecutor}.
     *
     * @param command the command to execute.
     * @param timeout execution timeout in milliseconds.
     * @param workingDirectory the working directory.
     */
    public CommandExecutor(String command, long timeout, String workingDirectory) {
        this.command = command;
        this.timeout = timeout;
        this.workingDirectory = new File(workingDirectory);
    }

    /**
     * Creates a new {@link CommandExecutor}.
     *
     * @param command the command to execute.
     * @param timeout execution timeout in milliseconds.
     * @param workingDirectory the working directory.
     * @param environmentParams the environment variables to set.
     */
    public CommandExecutor(String command, long timeout, String workingDirectory, String[] environmentParams) {
        this.command = command;
        this.timeout = timeout;
        this.workingDirectory = new File(workingDirectory);
        this.environmentParams = environmentParams;
    }

    /**
     * Executes the command.
     *
     * @return the {@link ExitStatus} of the command execution.
     * @throws Exception if command fails.
     */
    public ExitStatus execute() throws Exception {
        FutureTask<Integer> systemCommandTask = new FutureTask<Integer>(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                process = Runtime.getRuntime().exec(command, environmentParams, workingDirectory);
                return process.waitFor();
            }

        });

        long startTime = System.currentTimeMillis();

        taskExecutor.execute(systemCommandTask);

        while (true) {
            Thread.sleep(checkInterval);

            if (systemCommandTask.isDone()) {
                readOutput();

                return systemProcessExitCodeMapper.getExitStatus(systemCommandTask.get());
            } else if (System.currentTimeMillis() - startTime > timeout) {
                systemCommandTask.cancel(interruptOnCancel);
                throw new SystemCommandException("Execution of system command did not finish within the timeout.");
            }
        }
    }

    public String getCommand() {
        return command;
    }

    public String[] getEnvironmentParams() {
        return environmentParams;
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public long getTimeout() {
        return timeout;
    }

    public String[] getOutput() {
        return output.toArray(new String[] {});
    }

    /**
     * Reads the shell command output.
     *
     * @throws Exception if an I/O error occurs.
     */
    private void readOutput() throws Exception {
        output.clear();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line = "";
        while ((line = reader.readLine()) != null) {
            output.add(line);
        }
    }

}
