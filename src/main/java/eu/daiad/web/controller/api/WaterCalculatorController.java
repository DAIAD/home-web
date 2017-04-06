package eu.daiad.web.controller.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.model.AuthenticatedRequest;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.security.EnumRole;

/**
 * Provides actions for the water calculator.
 */
@RestController("ApiWaterCalculatorController")
public class WaterCalculatorController extends BaseRestController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(BillingController.class);

    /**
     * Logs an exception and creates a response.
     *
     * @param ex the exception.
     * @return an instance of {@link RestResponse}.
     */
    private RestResponse handleException(Exception ex) {
        logger.error(ex.getMessage(), ex);

        if (ex instanceof ApplicationException) {
            return new RestResponse(getError(ex));
        }

        return new RestResponse(getError(SharedErrorCode.UNKNOWN));
    }

    /**
     * Returns water breakdown data.
     *
     * @param request instance of {@link AuthenticatedRequest}.
     * @return a collection {@link WaterUse} objects.
     */
    @RequestMapping(value = "/api/v1/water-calculator/water-breakdown", method = RequestMethod.POST, produces = "application/json")
    public RestResponse getWaterUse(@RequestBody AuthenticatedRequest request) {
        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_USER, EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN);

            WaterUseCollection result = new WaterUseCollection();

            result.getLabels().add(new WaterUse("Shower", 40));
            result.getLabels().add(new WaterUse("Bath", 30));
            result.getLabels().add(new WaterUse("Washing machine", 15));
            result.getLabels().add(new WaterUse("Other", 15));

            return result;
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    public static final class WaterUse {

        private String label;

        private int percent;

        public WaterUse(String label, int percent) {
            this.label = label;
            this.percent = percent;
        }

        public String getLabel() {
            return label;
        }

        public int getPercent() {
            return percent;
        }

    }

    private static final class WaterUseCollection extends RestResponse {

        private List<WaterUse> labels = new ArrayList<WaterUse>();

        public WaterUseCollection() {
        }

        public List<WaterUse> getLabels() {
            return labels;
        }

    }

}
