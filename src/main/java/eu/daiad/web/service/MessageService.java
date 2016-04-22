package eu.daiad.web.service;

import eu.daiad.web.model.query.MessageAggregatesContainer;
import eu.daiad.web.model.recommendation.MessageCalculationConfiguration;
import eu.daiad.web.repository.application.IMessageManagementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Entry point for message calculation process.
 * 
 * @author nkarag
 */

@Service
public class MessageService implements IMessageService{
    
    @Autowired
    IMessageManagementRepository jpaMessageManagementRepository;

    @Autowired
    IAggregatesService messageAggregatesService;  
        
    @Override
    public void execute(MessageCalculationConfiguration config) {
        
        MessageAggregatesContainer aggregatesContainer = messageAggregatesService.execute(config);   
        //aggregatesContainer.resetValues();
        System.out.println(aggregatesContainer.toString());

        jpaMessageManagementRepository.testMethodCreateMessagesForDummyUser(config);
        //jpaMessageManagementRepository.execute(config);
    }

    @Override
    public void cancel() {
        if(messageAggregatesService.isRunning()){
            messageAggregatesService.cancel();
        }
        jpaMessageManagementRepository.cancel();
        
    }

    @Override
    public boolean isCancelled() {
        return false;
    }
  
}
