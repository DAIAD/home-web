package eu.daiad.web.model.message;

import eu.daiad.web.model.RestResponse;
import java.util.List;

public class RecommendationReceiversResponse extends RestResponse{
    
    private List<ReceiverAccount> receivers;
    private Recommendation recommendation;

    public List<ReceiverAccount> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<ReceiverAccount> receivers) {
        this.receivers = receivers;
    }

    public Recommendation getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(Recommendation recommendation) {
        this.recommendation = recommendation;
    }

}