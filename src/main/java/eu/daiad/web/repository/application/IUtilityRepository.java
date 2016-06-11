package eu.daiad.web.repository.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.daiad.web.model.admin.Counter;
import eu.daiad.web.model.utility.UtilityInfo;

public interface IUtilityRepository {

    abstract List<UtilityInfo> getUtilities();

    abstract UtilityInfo getUtilityById(int id);

    abstract UtilityInfo getUtilityByKey(UUID key);

    abstract Map<String, Counter> getCounters(int id);

}