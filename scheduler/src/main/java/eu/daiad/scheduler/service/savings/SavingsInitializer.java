package eu.daiad.scheduler.service.savings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import eu.daiad.common.repository.application.IBudgetRepository;
import eu.daiad.common.repository.application.ISavingsPotentialRepository;

/**
 * Resets status for all savings scenarios and budgets whose processing has been
 * interrupted.
 */
@Component
public class SavingsInitializer implements CommandLineRunner {

    /**
     * Repository for accessing savings scenario data.
     */
    @Autowired
    private ISavingsPotentialRepository savingsPotentialRepository;

    /**
     * Repository for accessing budget data.
     */
    @Autowired
    private IBudgetRepository budgetRepository;

    /**
     * Initializes security configuration.
     */
    @Override
    public void run(String... args) throws Exception {
        budgetRepository.cleanStatus();
        savingsPotentialRepository.cleanStatus();
    }

}
