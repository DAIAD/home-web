package eu.daiad.utility.controller.action;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.utility.controller.BaseController;
import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.model.security.RoleConstant;

/**
 * Provides actions for the water calculator.
 */
@RestController
public class WaterCalculatorController extends BaseController {

    /**
     * Returns water breakdown data.
     *
     * @param user the authenticated user.
     * @return a collection {@link WaterUse} objects.
     */
    @RequestMapping(value = "/action/water-calculator/water-breakdown", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER, RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getWaterUse(@AuthenticationPrincipal AuthenticatedUser user) {
        WaterUseCollection result = new WaterUseCollection();

        result.getLabels().add(new WaterUse("Shower", 40));
        result.getLabels().add(new WaterUse("Bath", 30));
        result.getLabels().add(new WaterUse("Washing machine", 15));
        result.getLabels().add(new WaterUse("Other", 15));

        return result;
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
