package eu.daiad.web.model.query.savings;

import java.io.IOException;
import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.web.domain.application.SavingsPotentialScenarioEntity;

public class SavingScenario {

    private UUID key;

    private UUID utilityKey;

    private String owner;

    private String name;

    private TemporalSavingsConsumerSelectionFilter parameters;

    private Double potential;

    private Double percent;

    private Double consumption;

    private DateTime createdOn;

    private DateTime processingBeginOn;

    private DateTime processingEndOn;

    private EnumSavingScenarioStatus status;

    public SavingScenario(SavingsPotentialScenarioEntity entity, ObjectMapper objectMapper) throws JsonParseException, JsonMappingException, IOException {
        key = entity.getKey();
        utilityKey = entity.getUtility().getKey();
        owner = entity.getOwner().getUsername();
        name = entity.getName();
        parameters = objectMapper.readValue(entity.getParameters(), new TypeReference<TemporalSavingsConsumerSelectionFilter>(){});
        potential = entity.getSavingsVolume();
        percent = entity.getSavingsPercent();
        consumption = entity.getConsumption();
        createdOn = entity.getCreatedOn();
        processingBeginOn = entity.getProcessingDateBegin();
        processingEndOn = entity.getProcessingDateEnd();
        status = entity.getStatus();
    }

    public UUID getKey() {
        return key;
    }

    public UUID getUtilityKey() {
        return utilityKey;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public TemporalSavingsConsumerSelectionFilter getParameters() {
        return parameters;
    }

    public Double getPotential() {
        return potential;
    }

    public Double getPercent() {
        return percent;
    }

    public Double getConsumption() {
        return consumption;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public DateTime getProcessingBeginOn() {
        return processingBeginOn;
    }

    public DateTime getProcessingEndOn() {
        return processingEndOn;
    }

    public EnumSavingScenarioStatus getStatus() {
        return status;
    }

}
