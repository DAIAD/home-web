package eu.daiad.common.repository.application;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.common.domain.application.PriceBracketEntity;
import eu.daiad.common.domain.application.UtilityEntity;
import eu.daiad.common.model.billing.PriceBracket;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.utility.UtilityInfo;
import eu.daiad.common.repository.BaseRepository;

@Repository
@Transactional
public class JpaBillingRepository extends BaseRepository implements IBillingRepository {

    /**
     * Entity manager for persisting data.
     */
    @PersistenceContext
    EntityManager entityManager;


    /**
     * Gets the currently applicable price brackets.
     *
     * @param utilityId the utility Id.
     * @return a list of {@link PriceBracket} objects.
     */
    @Override
    public List<PriceBracket> getPriceBracketByUtilityId(int utilityId) {
        UtilityInfo utility = getUtilityById(utilityId);

        DateTime now = new DateTime().toDateTime(DateTimeZone.forID(utility.getTimezone()));
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");

        return this.getPriceBracketByUtilityId(utilityId, now.toString(formatter));
    }

    /**
     * Gets the currently applicable price brackets.
     *
     * @param utilityId the utility Id.
     * @param referenceDate a reference date for selecting price brackets for a specific time interval.
     * @return a list of {@link PriceBracket} objects.
     */
    @Override
    public List<PriceBracket> getPriceBracketByUtilityId(int utilityId, String referenceDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
            formatter.parseDateTime(referenceDate);

            String queryString = "select b from price_bracket b where b.from <= :ref and :ref <= b.to and b.utility.id = :utilityId order by b.volume";

            TypedQuery<PriceBracketEntity> query = entityManager.createQuery(queryString, PriceBracketEntity.class)
                                                                .setParameter("utilityId", utilityId)
                                                                .setParameter("ref", referenceDate);

            List<PriceBracketEntity> result = query.getResultList();

            List<PriceBracket> brackets = new ArrayList<PriceBracket>();

            double min = 0;
            Double max = 0D;

            for (int index = 0, count = result.size(); index < count; index++) {
                min = (index == 0 ? 0 : max);
                max = (index == result.size() - 1 ? null : result.get(index).getVolume());

                if (max == null) {
                    brackets.add(new PriceBracket(min, result.get(index).getPrice()));
                } else {
                    brackets.add(new PriceBracket(min, max, result.get(index).getPrice()));
                }
            }

            return brackets;
        } catch (IllegalArgumentException ex) {
            throw createApplicationException(SharedErrorCode.INVALID_DATE_FORMAT);
        }
    }

    private  UtilityInfo getUtilityById(int id) {
        String queryString = "SELECT u FROM utility u where u.id = :id";

        TypedQuery<UtilityEntity> utilityQuery = entityManager.createQuery(queryString, UtilityEntity.class)
                                                              .setParameter("id", id);

        return new UtilityInfo(utilityQuery.getSingleResult());
    }

}
