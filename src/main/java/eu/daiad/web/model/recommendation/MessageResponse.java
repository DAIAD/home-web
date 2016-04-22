package eu.daiad.web.model.recommendation;

import eu.daiad.web.model.RestResponse;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nkarag
 */
public class MessageResponse extends RestResponse {
	List<Recommendation> messages = new ArrayList<>();

	public List<Recommendation> getMessages() {
		return messages;
	}

	public void setMessages(List<Recommendation> messages) {
		this.messages = messages;
	} 
        
}
