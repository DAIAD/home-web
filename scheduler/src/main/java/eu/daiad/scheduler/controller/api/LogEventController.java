package eu.daiad.scheduler.controller.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.common.domain.application.LogEventEntity;
import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.logging.LogEvent;
import eu.daiad.common.model.logging.LogEventQuery;
import eu.daiad.common.model.logging.LogEventQueryRequest;
import eu.daiad.common.model.logging.LogEventQueryResponse;
import eu.daiad.common.model.logging.LogEventQueryResult;
import eu.daiad.common.repository.application.ILogEventRepository;
import eu.daiad.scheduler.controller.BaseController;

/**
 * Provides methods for querying log events.
 */
@RestController
public class LogEventController extends BaseController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(LogEventController.class);

    /**
     * Repository for accessing log events.
     */
    @Autowired
    private ILogEventRepository logEventRepository;

    /**
     * Returns application events.
     *
     * @param request query for filtering log events.
     * @return a list of log events.
     */
    @PostMapping(value = "/api/v1/admin/logging/events", consumes = "application/json", produces = "application/json")
    public RestResponse getEvents(@RequestBody LogEventQueryRequest request) {
        try {
            // Set default values
            if (request.getQuery() == null) {
                request.setQuery(new LogEventQuery());
            }
            if ((request.getQuery().getIndex() == null) || (request.getQuery().getIndex() < 0)) {
                request.getQuery().setIndex(0);
            }
            if (request.getQuery().getSize() == null) {
                request.getQuery().setSize(10);
            }

            LogEventQueryResult result = logEventRepository.getLogEvents(request.getQuery());

            LogEventQueryResponse response = new LogEventQueryResponse();

            response.setTotal(result.getTotal());

            response.setIndex(request.getQuery().getIndex());
            response.setSize(request.getQuery().getSize());

            List<LogEvent> events = new ArrayList<LogEvent>();

            for (LogEventEntity entity : result.getEvents()) {
                LogEvent e = new LogEvent();

                e.setAccount(entity.getAccount());
                e.setCategory(entity.getCategory());
                e.setCode(entity.getCode());
                e.setId(entity.getId());
                e.setLevel(entity.getLevel());
                e.setLogger(entity.getLogger());
                e.setMessage(entity.getMessage());
                e.setRemoteAddress(entity.getRemoteAddress());
                e.setTimestamp(entity.getTimestamp().getMillis());

                events.add(e);
            }
            response.setEvents(events);

            return response;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

}
