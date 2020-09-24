package eu.daiad.common.model.utility;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ErrorCode;

public class UtilityInfoResponse extends RestResponse {

    private List<UtilityInfo> utilitiesInfo;

    public UtilityInfoResponse(UtilityInfo utilityInfo) {
        this.utilitiesInfo = new ArrayList<UtilityInfo>();

        if (utilityInfo != null) {
            this.utilitiesInfo.add(utilityInfo);
        }
    }

    public UtilityInfoResponse(List<UtilityInfo> utilitiesInfo) {
        this.utilitiesInfo = utilitiesInfo;
    }

    public UtilityInfoResponse(ErrorCode code, String description) {
        super(code, description);
    }

    public List<UtilityInfo> getUtilitiesInfo() {
        return utilitiesInfo;
    }
}