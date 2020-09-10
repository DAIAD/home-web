package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;

import eu.daiad.web.domain.application.LogEventEntity;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.logging.EnumLevel;
import eu.daiad.web.model.logging.LogEventQuery;
import eu.daiad.web.model.logging.LogEventQueryResult;
import eu.daiad.web.repository.BaseRepository;

@Repository
public class JpaLogEventRepository extends BaseRepository implements ILogEventRepository {

	@PersistenceContext
	EntityManager entityManager;

	/**
	 * Returns logged events. Optionally filters result based on the given
	 * query.
	 *
	 * @param query the query.
	 * @return the logged events.
	 */
	@Override
	public LogEventQueryResult getLogEvents(LogEventQuery query) {
		// Prepare response
		LogEventQueryResult result = new LogEventQueryResult();

		// Load data
		String command = "";

		try {
			// Resolve filters
			List<String> filters = new ArrayList<String>();

			if (!StringUtils.isBlank(query.getAccount())) {
				filters.add("e.account like :account");
			}
			if (query.getStartDate() != null) {
				filters.add("e.timestamp >= :start_date");
			}
			if (query.getEndDate() != null) {
				filters.add("e.timestamp <= :end_date");
			}
			if (!query.getLevel().equals(EnumLevel.UNDEFINED)) {
				filters.add("e.level = :level");
			}

			// Count total number of records
			Integer totalEvents;

			command = "select count(e.id) from log4j_message e ";
			if (!filters.isEmpty()) {
				command += "where " + StringUtils.join(filters, " and ");
			}

			TypedQuery<Number> countQuery = entityManager.createQuery(command, Number.class);

			if (!StringUtils.isBlank(query.getAccount())) {
				countQuery.setParameter("account", query.getAccount() + "%");
			}
			if (query.getStartDate() != null) {
				filters.add("e.timestamp >= :start_date");
				countQuery.setParameter("start_date", new DateTime(query.getStartDate()));
			}
			if (query.getEndDate() != null) {
				countQuery.setParameter("end_date", new DateTime(query.getEndDate()));
			}
			if (!query.getLevel().equals(EnumLevel.UNDEFINED)) {
				countQuery.setParameter("level", query.getLevel());
			}

			totalEvents = ((Number) countQuery.getSingleResult()).intValue();

			result.setTotal(totalEvents);

			// Load data
			command = "select e from log4j_message e ";
			if (!filters.isEmpty()) {
				command += "where " + StringUtils.join(filters, " and ");
			}
			command += " order by e.id desc ";

			TypedQuery<LogEventEntity> entityQuery = entityManager.createQuery(command, LogEventEntity.class);

			if (!StringUtils.isBlank(query.getAccount())) {
				entityQuery.setParameter("account", query.getAccount() + "%");
			}
			if (query.getStartDate() != null) {
				filters.add("e.timestamp >= :start_date");
				entityQuery.setParameter("start_date", new DateTime(query.getStartDate()));
			}
			if (query.getEndDate() != null) {
				entityQuery.setParameter("end_date", new DateTime(query.getEndDate()));
			}
			if (!query.getLevel().equals(EnumLevel.UNDEFINED)) {
				entityQuery.setParameter("level", query.getLevel());
			}

			entityQuery.setFirstResult(query.getIndex() * query.getSize());
			entityQuery.setMaxResults(query.getSize());

			result.setEvents(entityQuery.getResultList());

			return result;
		} catch (Exception ex) {
			throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
		}
	}
}
