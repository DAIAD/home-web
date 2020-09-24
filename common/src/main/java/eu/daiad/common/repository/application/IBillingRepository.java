package eu.daiad.common.repository.application;

import java.util.List;

import eu.daiad.common.domain.application.PriceBracketEntity;
import eu.daiad.common.model.billing.PriceBracket;

public interface IBillingRepository {

    /**
     * Gets the currently applicable price brackets.
     *
     * @param utilityId the utility Id.
     * @return a list of {@link PriceBracketEntity} entities.
     */
    List<PriceBracket> getPriceBracketByUtilityId(int utilityId);

    /**
     * Gets the currently applicable price brackets.
     *
     * @param utilityId the utility Id.
     * @param referenceDate a reference date for selecting price brackets for a specific time interval.
     * @return a list of {@link PriceBracketEntity} entities.
     */
    List<PriceBracket> getPriceBracketByUtilityId(int utilityId, String referenceDate);

}
