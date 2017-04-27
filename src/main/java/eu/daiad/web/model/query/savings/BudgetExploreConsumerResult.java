package eu.daiad.web.model.query.savings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.BudgetSnapshotAccountEntity;

public class BudgetExploreConsumerResult {

    private UUID key;

    private String name;

    private List<BudgetConsumerSnapshot> months = new ArrayList<BudgetConsumerSnapshot>();

    public BudgetExploreConsumerResult(AccountEntity account, List<BudgetSnapshotAccountEntity> consumerSnapshots) {
        key = account.getKey();
        name = account.getUsername();

        for (BudgetSnapshotAccountEntity s : consumerSnapshots) {
            months.add(new BudgetConsumerSnapshot(s));
        }

    }

    public UUID getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public List<BudgetConsumerSnapshot> getMonths() {
        return months;
    }

}
