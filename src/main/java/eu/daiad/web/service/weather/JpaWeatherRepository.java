package eu.daiad.web.service.weather;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.UtilityEntity;
import eu.daiad.web.domain.application.WeatherDailyDataEntity;
import eu.daiad.web.domain.application.WeatherHourlyDataEntity;
import eu.daiad.web.domain.application.WeatherServiceEntity;
import eu.daiad.web.repository.BaseRepository;

@Repository
@Transactional("applicationTransactionManager")
public class JpaWeatherRepository extends BaseRepository implements IWeatherRepository {

    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Override
    public List<WeatherServiceEntity> getServices() {
        TypedQuery<WeatherServiceEntity> serviceQuery = entityManager.createQuery("select s from weather_service s",
                        WeatherServiceEntity.class);

        return serviceQuery.getResultList();
    }

    @Override
    public WeatherServiceEntity getServiceById(int serviceId) {
        TypedQuery<WeatherServiceEntity> serviceQuery = entityManager.createQuery(
                        "select s from weather_service s where s.id = :serviceId", WeatherServiceEntity.class)
                        .setFirstResult(0).setMaxResults(1);

        serviceQuery.setParameter("serviceId", serviceId);

        List<WeatherServiceEntity> services = serviceQuery.getResultList();

        if (!services.isEmpty()) {
            return services.get(0);
        }

        return null;
    }

    @Override
    public WeatherServiceEntity getServiceByName(String serviceName) {
        TypedQuery<WeatherServiceEntity> serviceQuery = entityManager.createQuery(
                        "select s from weather_service s where LOWER(s.name) = LOWER(:name)", WeatherServiceEntity.class)
                        .setFirstResult(0).setMaxResults(1);

        serviceQuery.setParameter("name", serviceName);

        List<WeatherServiceEntity> services = serviceQuery.getResultList();

        if (!services.isEmpty()) {
            return services.get(0);
        }

        return null;
    }


    @Override
    public void update(int serviceId, int utilityId, DateTime createdOn, List<DailyWeatherData> data) {
        for (DailyWeatherData day : data) {

            WeatherDailyDataEntity dayEntity = objectToEntity(serviceId, utilityId, createdOn, day);

            for (HourlyWeatherData hour : day.getHours()) {
                objectToEntity(dayEntity, hour);
            }
        }
    }

    private WeatherDailyDataEntity objectToEntity(int serviceId, int utilityId, DateTime createdOn,
                    DailyWeatherData data) {
        if (data == null) {
            return null;
        }

        boolean isNew = false;
        WeatherDailyDataEntity entity;

        TypedQuery<WeatherDailyDataEntity> dayQuery = entityManager
                        .createQuery("select d from weather_data_day d where d.date = :date and d.service.id = :serviceId and d.utility.id = :utilityId",
                                        WeatherDailyDataEntity.class).setFirstResult(0).setMaxResults(1);

        dayQuery.setParameter("date", data.getDate());
        dayQuery.setParameter("serviceId", serviceId);
        dayQuery.setParameter("utilityId", utilityId);

        List<WeatherDailyDataEntity> days = dayQuery.getResultList();

        if (days.isEmpty()) {
            entity = new WeatherDailyDataEntity();

            // TODO : Cache responses
            TypedQuery<UtilityEntity> utilityQuery = entityManager.createQuery(
                            "select u from utility u where u.id = :utilityId", UtilityEntity.class).setFirstResult(0)
                            .setMaxResults(1);

            utilityQuery.setParameter("utilityId", utilityId);

            entity.setUtility(utilityQuery.getSingleResult());

            TypedQuery<WeatherServiceEntity> serviceQuery = entityManager.createQuery(
                            "select s from weather_service s where s.id = :serviceId", WeatherServiceEntity.class)
                            .setFirstResult(0).setMaxResults(1);

            serviceQuery.setParameter("serviceId", serviceId);

            entity.setService(serviceQuery.getSingleResult());

            isNew = true;
        } else {
            entity = days.get(0);
        }

        if (!StringUtils.isBlank(data.getConditions())) {
            entity.setConditions(data.getConditions());
        }

        entity.setCreatedOn(createdOn);
        entity.setDate(data.getDate());

        if (data.getMaxHumidity() != null) {
            entity.setMaxHumidity(data.getMaxHumidity());
        }
        if (data.getMinHumidity() != null) {
            entity.setMinHumidity(data.getMinHumidity());
        }

        if (data.getMaxTemperature() != null) {
            entity.setMaxTemperature(data.getMaxTemperature());
        }
        if (data.getMinTemperature() != null) {
            entity.setMinTemperature(data.getMinTemperature());
        }
        if (data.getMaxTemperatureFeel() != null) {
            entity.setMaxTemperatureFeel(data.getMaxTemperatureFeel());
        }
        if (data.getMinTemperatureFeel() != null) {
            entity.setMinTemperatureFeel(data.getMinTemperatureFeel());
        }

        if (data.getPrecipitation() != null) {
            entity.setPrecipitation(data.getPrecipitation());
        }

        if (!StringUtils.isBlank(data.getWindDirection())) {
            entity.setWindDirection(data.getWindDirection());
        }
        if (data.getWindSpeed() != null) {
            entity.setWindSpeed(data.getWindSpeed());
        }

        if (isNew) {
            entityManager.persist(entity);
            entityManager.flush();
        }
        return entity;
    }

    private WeatherHourlyDataEntity objectToEntity(WeatherDailyDataEntity day, HourlyWeatherData data) {
        if (data == null) {
            return null;
        }

        boolean isNew = false;
        WeatherHourlyDataEntity entity;

        TypedQuery<WeatherHourlyDataEntity> hourQuery = entityManager
                        .createQuery("select h from weather_data_hour h "
                                        + "where h.datetime = :datetime and h.day.service.id = :serviceId and h.day.utility.id = :utilityId",
                                        WeatherHourlyDataEntity.class).setFirstResult(0).setMaxResults(1);

        hourQuery.setParameter("datetime", data.getDatetime());
        hourQuery.setParameter("serviceId", day.getService().getId());
        hourQuery.setParameter("utilityId", day.getUtility().getId());

        List<WeatherHourlyDataEntity> hours = hourQuery.getResultList();

        if (hours.isEmpty()) {
            entity = new WeatherHourlyDataEntity();
            entity.setDay(day);

            isNew = true;
        } else {
            entity = hours.get(0);
        }

        if (!StringUtils.isBlank(data.getConditions())) {
            entity.setConditions(data.getConditions());
        }

        entity.setDatetime(data.getDatetime());

        if (data.getHumidity() != null) {
            entity.setHumidity(data.getHumidity());
        }
        if (data.getTemperature() != null) {
            entity.setTemperature(data.getTemperature());
        }
        if (data.getTemperatureFeel() != null) {
            entity.setTemperatureFeel(data.getTemperatureFeel());
        }
        if (data.getPrecipitation() != null) {
            entity.setPrecipitation(data.getPrecipitation());
        }

        if (!StringUtils.isBlank(data.getWindDirection())) {
            entity.setWindDirection(data.getWindDirection());
        }
        if (data.getWindSpeed() != null) {
            entity.setWindSpeed(data.getWindSpeed());
        }

        if (isNew) {
            entityManager.persist(entity);
        }
        return entity;
    }

    @Override
    public List<HourlyWeatherData> getHourlyData(int serviceId, int utilityId, DateTime from, DateTime to) {
        List<HourlyWeatherData> hours = new ArrayList<HourlyWeatherData>();

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");

        TypedQuery<WeatherHourlyDataEntity> hourQuery = entityManager
                        .createQuery("select h from weather_data_hour h "
                                        + "where h.datetime >= :from and h.datetime <= :to and h.day.service.id = :serviceId and h.day.utility.id = :utilityId",
                                        WeatherHourlyDataEntity.class);

        hourQuery.setParameter("from", from.toString(formatter) + "00");
        hourQuery.setParameter("to", to.toString(formatter) + "24");
        hourQuery.setParameter("serviceId", serviceId);
        hourQuery.setParameter("utilityId", utilityId);

        for (WeatherHourlyDataEntity entity : hourQuery.getResultList()) {
            HourlyWeatherData hour = new HourlyWeatherData(entity.getDatetime());

            hour.setConditions(entity.getConditions());
            hour.setHumidity(entity.getHumidity());
            hour.setPrecipitation(entity.getPrecipitation());
            hour.setTemperature(entity.getTemperature());
            hour.setTemperatureFeel(entity.getTemperatureFeel());
            hour.setWindDirection(entity.getWindDirection());
            hour.setWindSpeed(entity.getWindSpeed());

            hours.add(hour);
        }

        return hours;
    }

    @Override
    public List<DailyWeatherData> getDailyData(int serviceId, int utilityId, DateTime from, DateTime to) {
        List<DailyWeatherData> days = new ArrayList<DailyWeatherData>();

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");

        TypedQuery<WeatherDailyDataEntity> dayQuery = entityManager
                        .createQuery("select d from weather_data_day d "
                                        + "where d.date >= :from and d.date <= :to and d.service.id = :serviceId and d.utility.id = :utilityId",
                                        WeatherDailyDataEntity.class);

        dayQuery.setParameter("from", from.toString(formatter));
        dayQuery.setParameter("to", to.toString(formatter));
        dayQuery.setParameter("serviceId", serviceId);
        dayQuery.setParameter("utilityId", utilityId);

        for (WeatherDailyDataEntity entity : dayQuery.getResultList()) {
            DailyWeatherData day = new DailyWeatherData(entity.getDate());

            day.setConditions(entity.getConditions());
            day.setMaxHumidity(entity.getMaxHumidity());
            day.setMinHumidity(entity.getMinHumidity());
            day.setMaxTemperature(entity.getMaxTemperature());
            day.setMinTemperature(entity.getMinTemperature());
            day.setMaxTemperatureFeel(entity.getMaxTemperatureFeel());
            day.setMinTemperatureFeel(entity.getMinTemperatureFeel());
            day.setPrecipitation(entity.getPrecipitation());
            day.setWindDirection(entity.getWindDirection());
            day.setWindSpeed(entity.getWindSpeed());

            days.add(day);
        }

        return days;
    }

}
