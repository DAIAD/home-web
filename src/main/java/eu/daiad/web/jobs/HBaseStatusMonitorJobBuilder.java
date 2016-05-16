package eu.daiad.web.jobs;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.repository.application.HBaseConfigurationBuilder;

@Component
public class HBaseStatusMonitorJobBuilder implements IJobBuilder {

	private static final Log logger = LogFactory.getLog(HBaseStatusMonitorJobBuilder.class);

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private HBaseConfigurationBuilder configurationBuilder;

	public HBaseStatusMonitorJobBuilder() {

	}

	private static class StatusTasklet implements Tasklet {

		private HBaseConfigurationBuilder configurationBuilder;

		public StatusTasklet(HBaseConfigurationBuilder configurationBuilder) {
			this.configurationBuilder = configurationBuilder;
		}

		@Override
		public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
			Connection connection = null;
			Admin admin = null;

			try {
				Configuration config = this.configurationBuilder.build();

				connection = ConnectionFactory.createConnection(config);

				admin = connection.getAdmin();

				ClusterStatus status = admin.getClusterStatus();

				Collection<ServerName> servers = status.getDeadServerNames();

				if (servers.size() != 0) {
					for (ServerName name : servers) {
						logger.error(String.format("Server [%s] is down.", name.toString()));
					}
				}

			} catch (Exception ex) {
				logger.fatal("HBASE Master node is offline or could not be reached.", ex);

				throw ApplicationException.wrap(ex);
			} finally {
				try {
					if (admin != null) {
						admin.close();
					}
					if ((connection != null) && (!connection.isClosed())) {
						connection.close();
					}
				} catch (Exception ex) {
					logger.error("Failed to release HBASE connection resources.", ex);
				}
			}
			return RepeatStatus.FINISHED;
		}

	}

	private Step getStatus() {
		return stepBuilderFactory.get("getStatus").tasklet(new StatusTasklet(this.configurationBuilder)).build();
	}

	@Override
	public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
		return jobBuilderFactory.get(name).incrementer(incrementer).start(getStatus()).build();
	}
}
