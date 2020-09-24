package eu.daiad.scheduler.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import eu.daiad.common.model.error.ErrorCode;
import eu.daiad.common.repository.application.JpaUserRepository;

/**
 * During the application initialization verifies that all error codes are
 * unique.
 */
@Component
public class ErrorCodeInitializer implements CommandLineRunner {

	private static final Log logger = LogFactory.getLog(JpaUserRepository.class);

	@Override
	public void run(String... args) throws Exception {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(ErrorCode.class));

		Map<String, String> messages = new HashMap<String, String>();

		for (BeanDefinition bd : scanner.findCandidateComponents("eu.daiad")) {
			Class<?> c = Class.forName(bd.getBeanClassName());

			for (ErrorCode e : (ErrorCode[]) c.getEnumConstants()) {
				String code = e.getMessageKey();

				if (messages.containsKey(code)) {
					throw new Exception(
						String.format(
							"ErrorCode [%s] in class [%s] is already declared in class [%s].", code,
							bd.getBeanClassName(), messages.get(code)
						)
					);
				} else {
					messages.put(code, bd.getBeanClassName());
					logger.debug(String.format("Class : [%s] Message Key : [%s]", bd.getBeanClassName(), code));
				}
			}
		}
	}
}
