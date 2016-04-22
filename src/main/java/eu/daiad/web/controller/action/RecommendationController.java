package eu.daiad.web.controller.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.query.MessageAggregatesContainer;
import eu.daiad.web.model.recommendation.MessageCalculationConfiguration;
import eu.daiad.web.model.recommendation.StaticRecommendationResponse;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IRecommendationRepository;
import eu.daiad.web.service.IAggregatesService;
import eu.daiad.web.service.IMessageService;


@RestController
public class RecommendationController extends BaseController {

	private static final Log logger = LogFactory.getLog(RecommendationController.class);

	@Autowired
	private IRecommendationRepository recommendationRepository;

        @Autowired
        IAggregatesService messageAggregatesService;   
        
        @Autowired
        IMessageService messageMessageService;  
        
	@RequestMapping(value = "/action/recommendation/static/{locale}", method = RequestMethod.GET, produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse getRecommendations(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String locale) {
		StaticRecommendationResponse response = new StaticRecommendationResponse();

		try {
			response.setRecommendations(this.recommendationRepository.getStaticRecommendations(locale));
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}  
        
	@RequestMapping(value = "/utility/e", method = RequestMethod.GET, produces = "application/json")
	@Secured("ROLE_ADMIN")
	public RestResponse execute(@AuthenticationPrincipal AuthenticatedUser user) {
            RestResponse response = new RestResponse();      
            try {
                MessageCalculationConfiguration config = new MessageCalculationConfiguration();
                MessageAggregatesContainer aggregatesContainer = messageAggregatesService.execute(config);   
                //aggregatesContainer.resetValues();
                System.out.println( aggregatesContainer.toString());
                messageMessageService.execute(config);


            } catch (ApplicationException ex) {
                    logger.error(ex);
                    response.add(this.getError(ex));
            }
            response.add("execute", "success");
            return response;
	}         
        
	@RequestMapping(value = "/ack/{type}/{stringId}", method = RequestMethod.GET, produces = "application/json")
	@Secured("ROLE_ADMIN")
	public RestResponse acknowledgeMessage
        (@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String type, @PathVariable String stringId) {
            
            RestResponse response = new RestResponse();             
            int id = Integer.parseInt(stringId);
            try{               
                //jpaMessageRepository.messageAcknowledged("user",type,  id, DateTime.now());
                
            }
            catch(ApplicationException ex){
                logger.error(ex);

                response.add(this.getError(ex));                
            }
            response.add("acknowledgement", "success");
            return response;
	}         
        
}
