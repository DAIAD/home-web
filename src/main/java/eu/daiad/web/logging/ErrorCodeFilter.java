package eu.daiad.web.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

import eu.daiad.web.model.error.ApplicationException;

@Plugin(name = "ErrorCodeFilter", category = Node.CATEGORY, elementType = Filter.ELEMENT_TYPE, printObject = true)
public class ErrorCodeFilter extends AbstractFilter {

	private static final long serialVersionUID = 5708767168420025448L;

	private final List<String> categories;

	protected ErrorCodeFilter(final List<String> categories) {
		super(Result.ACCEPT, Result.DENY);
		Objects.requireNonNull(categories, "categories cannot be null");
		this.categories = categories;
	}

	@Override
	public Result filter(final Logger logger, final Level level, final Marker marker, final Message msg,
					final Throwable t) {
		if (t instanceof ApplicationException) {
			if (categories.contains(((ApplicationException) t).getCode().getClass().getSimpleName())) {
				return Result.ACCEPT;
			}
			return Result.DENY;
		}
		return Result.NEUTRAL;
	}

	@Override
	public Result filter(final LogEvent event) {
		final Throwable t = event.getThrown();
		if (t == null) {
			if (event.getLevel().isMoreSpecificThan(Level.INFO)) {
				return Result.NEUTRAL;
			} else {
				return Result.DENY;
			}
		}
		if (t instanceof ApplicationException) {
			if ((categories.isEmpty())
							|| (categories.contains(((ApplicationException) t).getCode().getClass().getSimpleName()))) {
				return Result.ACCEPT;
			}
			return Result.DENY;
		}
		return Result.NEUTRAL;
	}

	@Override
	public Result filter(final Logger logger, final Level level, final Marker marker, final Object msg,
					final Throwable t) {
		if (t instanceof ApplicationException) {
			if (categories.contains(((ApplicationException) t).getCode().getClass().getSimpleName())) {
				return Result.ACCEPT;
			}
			return Result.DENY;
		}
		return Result.NEUTRAL;
	}

	@Override
	public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
					final Object... params) {
		if (level.isMoreSpecificThan(Level.WARN)) {
			return Result.ACCEPT;
		}
		return Result.NEUTRAL;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("categories=");
		if (categories.size() > 0) {
			boolean first = true;
			for (final String entry : categories) {
				if (!first) {
					sb.append(", ");
				}
				first = false;
				sb.append(entry);
			}
		}
		return sb.toString();
	}

	protected List<String> getCategories() {
		return categories;
	}

	@PluginFactory
	public static ErrorCodeFilter createFilter(@PluginAttribute("categories") final String values) {
		final List<String> list = new ArrayList<String>();

		if (StringUtils.isBlank(values)) {
			LOGGER.warn("No categories set for the ErrorCodeFilter. All error codes will be logged");
		} else {
			String[] categories = StringUtils.split(values, ',');

			for (final String category : categories) {
				if (StringUtils.isBlank(category)) {
					LOGGER.error("A null category is not valid in ErrorCodeFilter");
					continue;
				}

				list.add(category.trim());
			}
			if (list.isEmpty()) {
				LOGGER.warn("No categories set for the ErrorCodeFilter. All error codes will be logged");
			}
		}
		return new ErrorCodeFilter(list);
	}
}
