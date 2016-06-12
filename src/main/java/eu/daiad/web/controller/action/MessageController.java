package eu.daiad.web.controller.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.message.AnnouncementRequest;
import eu.daiad.web.model.message.EnumMessageType;
import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.MessageAcknowledgementRequest;
import eu.daiad.web.model.message.MessageRequest;
import eu.daiad.web.model.message.MessageResult;
import eu.daiad.web.model.message.MultiTypeMessageResponse;
import eu.daiad.web.model.message.SingleTypeMessageResponse;
import eu.daiad.web.model.message.StaticRecommendation;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IMessageRepository;
import java.util.List;

/**
 * Provides actions for loading messages and saving acknowledgments.
 *
 */
@RestController
public class MessageController extends BaseController {

	private static final Log logger = LogFactory.getLog(MessageController.class);

	@Autowired
	private IMessageRepository messageRepository;

	/**
	 * Loads messages i.e. alerts, recommendations and tips. Optionally filters messages.
	 * 
	 * @param request the request.
	 * @return the messages.
	 */
	@RequestMapping(value = "/action/message", method = RequestMethod.POST, produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse getMessages(@RequestBody MessageRequest request) {
		try {
			MultiTypeMessageResponse messageResponse = new MultiTypeMessageResponse();

			MessageResult result = this.messageRepository.getMessages(request);

			messageResponse.setTotalAlerts(result.getTotalAlerts());
			messageResponse.setTotalAnnouncements(result.getTotalAnnouncements());
			messageResponse.setTotalRecommendations(result.getTotalRecommendations());
			messageResponse.setTotalTips(result.getTotalTips());

			for (Message message : result.getMessages()) {
				switch (message.getType()) {
					case ALERT:
						messageResponse.getAlerts().add(message);
						break;
					case RECOMMENDATION_STATIC:
						messageResponse.getTips().add(message);
						break;
					case RECOMMENDATION_DYNAMIC:
						messageResponse.getRecommendations().add(message);
						break;
					case ANNOUNCEMENT:
						messageResponse.getAnnouncements().add(message);
						break;
					default:
						// Ignore
				}
			}

			return messageResponse;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			RestResponse response = new RestResponse();
			response.add(this.getError(ex));
			return response;
		}
	}

	/**
	 * Saves one or more message acknowledgments.
	 * 
	 * @param request the messages to acknowledge.
	 * @return the controller response.
	 */
	@RequestMapping(value = "/action/message/acknowledge", method = RequestMethod.POST, produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse acknowledgeMessage(@RequestBody MessageAcknowledgementRequest request) {
		RestResponse response = new RestResponse();

		try {
			this.messageRepository.setMessageAcknowledgement(request.getMessages());
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}
		return response;
	}

	/**
	 * Gets static localized recommendations (tips) based on locale.
	 * 
	 * @param user the user
	 * @param locale the locale
	 * @return the static recommendations.
	 */
	@RequestMapping(value = "/action/recommendation/static/{locale}", method = RequestMethod.GET, produces = "application/json")
	@Secured("ROLE_ADMIN")
	public RestResponse getRecommendations(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String locale) {
		try {
			SingleTypeMessageResponse messages = new SingleTypeMessageResponse();

			messages.setType(EnumMessageType.RECOMMENDATION_STATIC);
			messages.setMessages(this.messageRepository.getAdvisoryMessages(locale));

			return messages;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			RestResponse response = new RestResponse();
			response.add(this.getError(ex));
			return response;
		}
	}

    /**
     * Activate/Deactivate the received recommendations (tips).
     * 
     * @param request the messages to change activity status
     * @return the controller response.
     */
    @RequestMapping(value = "/action/recommendation/static/status/save/", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_ADMIN")
	public RestResponse setStaticTipsActivityStatusChange(@RequestBody List<StaticRecommendation> request) {
        RestResponse response = new RestResponse();
        try {
        
            for(StaticRecommendation st : request){
                this.messageRepository.persistAdvisoryMessageActiveStatus(st.getId(), st.isActive());
            }
            
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			response.add(this.getError(ex));
			return response;
		}
        return response;
	}    
    
    /**
     * Add a new or edit an existing recommendation (tip).
     * 
     * @param request the message to add or edit
     * @return the controller response.
     */
    @RequestMapping(value = "/action/recommendation/static/insert", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_ADMIN")
	public RestResponse insertStaticRemmendation(@RequestBody StaticRecommendation request) {
        RestResponse response = new RestResponse();
        
        try {
        
            if(request.getId() == 0 || request.getIndex() == 0){           
                this.messageRepository.persistNewAdvisoryMessage(request);
            }
            else{            
                this.messageRepository.updateAdvisoryMessage(request);
            }

		} catch (Exception ex) {
			    logger.error(ex.getMessage(), ex);
			    response.add(this.getError(ex));
			    return response;
		}
        return response;
	}    
    
    /**
     * Delete an existing recommendation (tip).
     * 
     * @param request the message to add or edit
     * @return the controller response.
     */
    @RequestMapping(value = "/action/recommendation/static/delete", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_ADMIN")
	public RestResponse deleteStaticRemmendation(@RequestBody StaticRecommendation request) {
        RestResponse response = new RestResponse();
        
        try {
            this.messageRepository.deleteAdvisoryMessage(request);
            
		} catch (Exception ex) {
	        logger.error(ex.getMessage(), ex);
		    response.add(this.getError(ex));
		    return response;
		}
        return response;
	}  
    
    /**
     * Send announcement to the provided accounts.
     * 
     * @param user the user.
     * @param request the request containing the announcement and the receivers.
     * @return the controller response.
     */
    @RequestMapping(value = "/action/announcement/broadcast", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_ADMIN")
	public RestResponse broadCastAnnouncement(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody AnnouncementRequest request) {
        RestResponse response = new RestResponse();
        
        try {

            String channel = "web";
            this.messageRepository.broadcastAnnouncement(request, user.getLocale(), channel); //default channel web

		} catch (Exception ex) {
			    logger.error(ex.getMessage(), ex);
			    response.add(this.getError(ex));
			    return response;
		}
        return response;
	}    
    
	/**
	 * Get localized announcements history.
	 * 
	 * @param user the user
	 * @return the announcements.
	 */
	@RequestMapping(value = "/action/announcements/history", method = RequestMethod.GET, produces = "application/json")
	@Secured("ROLE_ADMIN")
	public RestResponse getAnnouncements(@AuthenticationPrincipal AuthenticatedUser user) {
		try {
			SingleTypeMessageResponse messages = new SingleTypeMessageResponse();

			messages.setType(EnumMessageType.ANNOUNCEMENT);
			messages.setMessages(this.messageRepository.getAnnouncements(user.getLocale()));

			return messages;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			RestResponse response = new RestResponse();
			response.add(this.getError(ex));
			return response;
		}
	}    
    
}
