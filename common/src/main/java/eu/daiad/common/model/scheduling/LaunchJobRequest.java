package eu.daiad.common.model.scheduling;

import java.util.HashMap;
import java.util.Map;

import eu.daiad.common.model.AuthenticatedRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LaunchJobRequest extends AuthenticatedRequest {

	@Getter
	private Map<String, String> parameters = new HashMap<>();

	public LaunchJobRequest param(String key, String value) {
		this.parameters.put(key, value);

		return this;
	}
}
