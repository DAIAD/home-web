package eu.daiad.web.job.task;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import eu.daiad.web.mapreduce.EnumJobMapReduceParameter;
import eu.daiad.web.mapreduce.RunJar;
import eu.daiad.web.model.error.SchedulerErrorCode;

/**
 * Task for submitting a MapReduce job to a YARN cluster.
 */
@Component
public class YarnMapReduceJobTask extends BaseTask implements StoppableTasklet {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(YarnMapReduceJobTask.class);

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        String jobName = "";

        try {
            logPermGenUsage();

            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            jobName = parameters.get(EnumJobMapReduceParameter.JOB_NAME.getValue());

            RunJar runJar = new RunJar();

            runJar.run(parameters);

            logPermGenUsage();
        } catch (Throwable t) {
            logPermGenUsage();

            throw wrapApplicationException(t, SchedulerErrorCode.MAPREDUCE_JOB_INIT_FAILED).set("job", jobName);
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {
        // TODO: Add business logic for stopping processing
    }


    private void logPermGenUsage() {
        try {
            MemoryPoolMXBean permgenBean = null;

            List<MemoryPoolMXBean> beans = ManagementFactory.getMemoryPoolMXBeans();
            for (MemoryPoolMXBean bean : beans) {
                if (bean.getName().toLowerCase().indexOf("perm gen") >= 0) {
                    permgenBean = bean;
                    break;
                }
            }

            if (permgenBean != null) {
                MemoryUsage currentUsage = permgenBean.getUsage();
                int usage =  (int) ((currentUsage.getUsed() * 100) / currentUsage.getMax());

                logger.info(String.format("Permgen %.2f of %.2f  (%d %%)",
                                          (float) currentUsage.getUsed() / 1024f,
                                          (float) currentUsage.getMax() / 1024f,
                                          usage));
            }
        } catch(Exception ex) {
            logger.info("Failed to compute PermGen usage.", ex);
        }
    }
}
