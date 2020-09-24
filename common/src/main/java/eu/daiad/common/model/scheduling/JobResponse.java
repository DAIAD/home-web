package eu.daiad.common.model.scheduling;

import java.util.List;

import eu.daiad.common.model.RestResponse;
import lombok.Getter;
import lombok.Setter;

public class JobResponse extends RestResponse {

	@Setter
	@Getter
	private JobInfo job;

	@Getter
	@Setter
	private List<JobExecutionInfo> executions;

}
