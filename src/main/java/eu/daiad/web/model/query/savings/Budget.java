package eu.daiad.web.model.query.savings;

import java.io.IOException;
import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.web.domain.application.BudgetEntity;

public class Budget {

    private UUID key;

    private UUID utilityKey;

    private String owner;

    private String name;

    private BudgetParameters parameters;

    private Double consumptionBefore;

    private Double consumptionAfter;

    private Double savingsPercent;

    private Double expectedPercent;

    private DateTime createdOn;

    private DateTime updatedOn;

    private DateTime nextUpdateOn;

    private boolean active;

    private DateTime activatedOn;

    private Integer numberOfConsumers;

    private boolean initialized;

    public Budget(BudgetEntity entity, ObjectMapper objectMapper) throws JsonParseException, JsonMappingException,
                    IOException {
        key = entity.getKey();
        utilityKey = entity.getUtility().getKey();
        owner = entity.getOwner().getUsername();
        name = entity.getName();
        parameters = objectMapper.readValue(entity.getParameters(), new TypeReference<BudgetParameters>() { });
        consumptionBefore = entity.getConsumptionBefore();
        consumptionAfter = entity.getConsumptionAfter();
        savingsPercent = entity.getSavingsPercent();
        createdOn = entity.getCreatedOn();
        updatedOn = entity.getUpdatedOn();
        nextUpdateOn = entity.getNextUpdateOn();
        active = entity.isActive();
        activatedOn = entity.getActivatedOn();
        numberOfConsumers = entity.getNumberOfConsumers();
        initialized = entity.isInitialized();
        expectedPercent = entity.getExpectedPercent();
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

    public BudgetParameters getParameters() {
        return parameters;
    }

    public Double getConsumptionBefore() {
        return consumptionBefore;
    }

    public Double getConsumptionAfter() {
        return consumptionAfter;
    }

    public Double getSavingsPercent() {
        return savingsPercent;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public DateTime getUpdatedOn() {
        return updatedOn;
    }

    public DateTime getNextUpdateOn() {
        return nextUpdateOn;
    }

    public boolean isActive() {
        return active;
    }

    public DateTime getActivatedOn() {
        return activatedOn;
    }

    public Integer getNumberOfConsumers() {
        return numberOfConsumers;
    }

    public Double getExpectedPercent() {
        return expectedPercent;
    }

}
