package eu.daiad.common.model.query.savings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.daiad.common.model.RestResponse;

public class BudgetExploreConsumerResponse extends RestResponse {

    private UUID userKey;

    private String userName;

    private List<BudgetConsumerSnapshot> months = new ArrayList<BudgetConsumerSnapshot>();

    public BudgetExploreConsumerResponse(BudgetExploreConsumerResult result) {
        userKey = result.getKey();
        userName = result.getName();
        months = result.getMonths();
    }

    public UUID getUserKey() {
        return userKey;
    }

    public String getUserName() {
        return userName;
    }

    public List<BudgetConsumerSnapshot> getMonths() {
        return months;
    }

}
