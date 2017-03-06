package eu.daiad.web.controller.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.model.AuthenticatedRequest;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.ErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.message.Alert;
import eu.daiad.web.model.message.AlertCode;
import eu.daiad.web.model.message.Announcement;
import eu.daiad.web.model.message.EnumMessageType;
import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.MessageRequest;
import eu.daiad.web.model.message.MessageResult;
import eu.daiad.web.model.message.ReceiverAccount;
import eu.daiad.web.model.message.Recommendation;
import eu.daiad.web.model.message.Tip;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.Credentials;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.security.RoleConstant;
import eu.daiad.web.model.user.UserInfo;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IUtilityRepository;
import eu.daiad.web.service.message.IMessageService;
import eu.daiad.web.util.csv.RecordMapper;
import eu.daiad.web.util.csv.SimpleRecordMapper;

@RestController
public class DebugMessageController extends BaseRestController
{
    private static final Log logger = LogFactory.getLog(DebugMessageController.class);
    
    @Autowired
    private IUtilityRepository utilityRepository;
    
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IMessageService service;
    
    public static class ExportRequest extends AuthenticatedRequest
    {
        @JsonProperty
        @NotNull
        private EnumMessageType type;
        
        @JsonProperty
        @NotNull
        private UUID utility;
        
        @JsonProperty
        @Min(1)
        @Max(50)
        private int limit = 25;
        
        @JsonProperty
        private String locale;
        
        public EnumMessageType getType()
        {
            return type;
        }

        public UUID getUtility()
        {
            return utility;
        }

        public int getLimit()
        {
            return limit;
        }

        public String getLocale()
        {
            return locale == null? "en" : locale; 
        }
    }
    
    public static class PerAccountMessages 
    {
        @JsonProperty
        private ReceiverAccount receiver;
        
        @JsonProperty
        private List<Message> messages;

        public PerAccountMessages(ReceiverAccount receiver, List<Message> messages)
        {
            this.receiver = receiver;
            this.messages = messages;
        }

        public ReceiverAccount getReceiver()
        {
            return receiver;
        }

        public void setReceiver(ReceiverAccount receiver)
        {
            this.receiver = receiver;
        }

        public List<Message> getMessages()
        {
            return messages;
        }

        public void setMessages(List<Message> messages)
        {
            this.messages = messages;
        }
    }
    
    public static abstract class AccountMessage <M extends Message>
    {
        @JsonProperty
        private ReceiverAccount receiver;
        
        public ReceiverAccount getReceiver()
        {
            return receiver;
        }

        public void setReceiver(ReceiverAccount receiver)
        {
            this.receiver = receiver;
        }
        
        public abstract M getMessage();
        
        public abstract void setMessage(M message);
    }
    
    public static abstract class AccountMessagePrinter
    {
        public abstract String toLine(ReceiverAccount receiver, Message message) throws Exception;
        
        public abstract String toHeaderLine();
    }
      
    public static class AccountTip extends AccountMessage<Tip> 
    {
        private Tip message;
        
        @Override
        public Tip getMessage()
        {
            return message;
        }

        @Override
        public void setMessage(Tip message)
        {
            this.message = message;
        }
    }
    
    public static class AccountTipPrinter extends AccountMessagePrinter
    {
        private static final RecordMapper<AccountTip> recordMapper;
        
        private static final OrderedMap<String, String> fieldNames;
        
        static {
            fieldNames = new LinkedMap<String, String>() {{
                put("receiver.username", "User-Name");
                put("receiver.key", "User-Key");
                put("message.id", "Id");
                put("message.type", "Type");
                put("message.categoryName","Category-Name");
                put("message.title","Title");
                put("message.description","Description");
                put("message.createdOn","Created");
                put("message.acknowledgedOn","Acknowledged");
            }};
            recordMapper = new SimpleRecordMapper<>(AccountTip.class, fieldNames);
        }
        
        @Override
        public String toLine(ReceiverAccount receiver, Message message) throws Exception
        {
            AccountTip r = new AccountTip();
            r.setReceiver(receiver);
            r.setMessage((Tip) message);
            return recordMapper.toLine(r);
        }

        @Override
        public String toHeaderLine()
        {
            return recordMapper.toHeaderLine();
        } 
    }
    
    public static class AccountAlert extends AccountMessage<Alert> 
    {
        private Alert message;
        
        @Override
        public Alert getMessage()
        {
            return message;
        }

        @Override
        public void setMessage(Alert message)
        {
            this.message = message;
        }
    }
    
    public static class AccountAlertPrinter extends AccountMessagePrinter
    {
        private static final RecordMapper<AccountAlert> recordMapper;
        
        private static final OrderedMap<String, String> fieldNames;
        
        static {
            fieldNames = new LinkedMap<String, String>() {{
                put("receiver.username", "User-Name");
                put("receiver.key", "User-Key");
                put("message.id", "Id");
                put("message.type", "Type");
                put("message.alertType", "Alert-Type");
                put("message.alertCode", "Alert-Code");
                put("message.alertTemplate", "Alert-Template");
                put("message.title","Title");
                put("message.description","Description");
                put("message.refDate","Reference-Date");
                put("message.createdOn","Created");
                put("message.acknowledgedOn","Acknowledged");
            }};
            recordMapper = new SimpleRecordMapper<>(AccountAlert.class, fieldNames);
        }
        
        @Override
        public String toLine(ReceiverAccount receiver, Message message) throws Exception
        {
            AccountAlert r = new AccountAlert();
            r.setReceiver(receiver);
            r.setMessage((Alert) message);
            return recordMapper.toLine(r);
        }

        @Override
        public String toHeaderLine()
        {
            return recordMapper.toHeaderLine();
        } 
    }
    
    public static class AccountAnnouncement extends AccountMessage<Announcement> 
    {
        private Announcement message;
        
        @Override
        public Announcement getMessage()
        {
            return message;
        }

        @Override
        public void setMessage(Announcement message)
        {
            this.message = message;
        }
    }
    
    public static class AccountAnnouncementPrinter extends AccountMessagePrinter
    {
        private static final RecordMapper<AccountAnnouncement> recordMapper;
        
        private static final OrderedMap<String, String> fieldNames;
        
        static {
            fieldNames = new LinkedMap<String, String>() {{
                put("receiver.username", "User-Name");
                put("receiver.key", "User-Key");
                put("message.id", "Id");
                put("message.type", "Type");
                put("message.title","Title");
                put("message.content","Content");
                put("message.createdOn","Created");
                put("message.acknowledgedOn","Acknowledged");
            }};
            recordMapper = new SimpleRecordMapper<>(AccountAnnouncement.class, fieldNames);
        }
        
        @Override
        public String toLine(ReceiverAccount receiver, Message message) throws Exception
        {
            AccountAnnouncement r = new AccountAnnouncement();
            r.setReceiver(receiver);
            r.setMessage((Announcement) message);
            return recordMapper.toLine(r);
        }

        @Override
        public String toHeaderLine()
        {
            return recordMapper.toHeaderLine();
        } 
    }
    
    public static class AccountRecommendation extends AccountMessage<Recommendation> 
    {
        private Recommendation message;
        
        @Override
        public Recommendation getMessage()
        {
            return message;
        }

        @Override
        public void setMessage(Recommendation message)
        {
            this.message = message;
        }
    }
    
    public static class AccountRecommendationPrinter extends AccountMessagePrinter
    {
        private static final RecordMapper<AccountRecommendation> recordMapper;
        
        private static final OrderedMap<String, String> fieldNames;
        
        static {
            fieldNames = new LinkedMap<String, String>() {{
                put("receiver.username", "User-Name");
                put("receiver.key", "User-Key");
                put("message.id", "Id");
                put("message.type", "Type");
                put("message.recommendationType", "Recommendation-Type");
                put("message.recommendationCode", "Recommendation-Code");
                put("message.recommendationTemplate", "Recommendation-Template");
                put("message.title","Title");
                put("message.description","Description");
                put("message.refDate","Reference-Date");
                put("message.createdOn","Created");
                put("message.acknowledgedOn","Acknowledged");
            }};
            recordMapper = new SimpleRecordMapper<>(AccountRecommendation.class, fieldNames);
        }
        
        @Override
        public String toLine(ReceiverAccount receiver, Message message) throws Exception
        {
            AccountRecommendation r = new AccountRecommendation();
            r.setReceiver(receiver);
            r.setMessage((Recommendation) message);
            return recordMapper.toLine(r);
        }

        @Override
        public String toHeaderLine()
        {
            return recordMapper.toHeaderLine();
        } 
    }
    
    public static class ExportResponse extends RestResponse
    {
        public ExportResponse() {}
        
        public ExportResponse(eu.daiad.web.model.error.Error e) 
        {
            super(e);
        }
        
        @JsonProperty
        private List<PerAccountMessages> accounts = new ArrayList<>();
    }
    
    @RequestMapping(
        value = "/api/v1/message/export", 
        method = RequestMethod.POST, 
        consumes = "application/json", 
        produces = "application/json"
    )
    public RestResponse exportMessages(@RequestBody ExportRequest request)
    {
        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_SYSTEM_ADMIN);
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage());
            return new RestResponse(getError(ex));
        }
        
        ArrayList<eu.daiad.web.model.error.Error> errors = validate(request);
        if (!errors.isEmpty())
            return new RestResponse(errors);
        
        MessageRequest r = new MessageRequest(request.getLocale())
            .withOptions(new MessageRequest.Options(request.getType(), request.getLimit()));
        
        ExportResponse response = new ExportResponse();
        try {
            for (UUID accountKey: utilityRepository.getMembers(request.getUtility())) {                
                AuthenticatedUser user = userRepository.getUserByKey(accountKey);
                MessageResult q = service.getMessages(user, r);
                if (!q.getMessages().isEmpty()) {
                    ReceiverAccount receiver = ReceiverAccount.of(accountKey, user.getUsername());
                    response.accounts.add(new PerAccountMessages(receiver, q.getMessages()));
                }
            }
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);
            response = new ExportResponse(getError(ex));
        }
        
        return response;
    }
    
    @RequestMapping(
        value = "/api/v1/message/export.csv", 
        method = RequestMethod.POST, 
        consumes = "application/json", 
        produces = "text/csv"
    )
    public String exportMessagesToSpreadsheet(@RequestBody ExportRequest request) 
        throws IllegalAccessException
    {
        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_SYSTEM_ADMIN);
        } catch (ApplicationException ex) {
            throw new IllegalAccessException("Forbidden: " + ex.getMessage());
        }
        
        ArrayList<eu.daiad.web.model.error.Error> errors = validate(request);
        if (!errors.isEmpty())
            throw new ValidationException("The request is invalid");
       
        EnumMessageType messageType = request.getType();
        MessageRequest r = new MessageRequest(request.getLocale())
            .withOptions(new MessageRequest.Options(messageType, request.getLimit()));
        
        AccountMessagePrinter recordPrinter = null;
        switch (messageType) {
        case ALERT:
            recordPrinter = new AccountAlertPrinter();
            break;
        case RECOMMENDATION: 
            recordPrinter = new AccountRecommendationPrinter();
            break;
        case ANNOUNCEMENT:
            recordPrinter = new AccountAnnouncementPrinter();
            break;
        case TIP:
            recordPrinter = new AccountTipPrinter();
            break;
        default:
            Assert.state(false, "Unknown message-type");
        }
        
        StringBuilder outs = new StringBuilder();
        outs.append(recordPrinter.toHeaderLine() + "\n");
        
        for (UUID accountKey: utilityRepository.getMembers(request.getUtility())) {
            AuthenticatedUser user = userRepository.getUserByKey(accountKey);
            MessageResult q = service.getMessages(user, r);
            List<Message> messages = q.getMessages();
            if (messages.isEmpty())
                continue; // skip; no messages for this user
            ReceiverAccount receiver = ReceiverAccount.of(accountKey, user.getUsername());
            for (Message message: messages) {
                String line; 
                try {
                    line = recordPrinter.toLine(receiver, message);
                } catch (Exception ex) {
                    line = null;
                    logger.info(
                        "Failed to export message #" + message.getId() + ": " +
                        ex.getMessage());
                }
                if (line != null)
                    outs.append(line + "\n");
            }
            outs.append("\n");
        }
      
        return outs.toString();
    }
    
    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public String handleException(ValidationException ex)
    {
        return ex.getMessage();
    }
    
    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.FORBIDDEN)
    public String handleException(IllegalAccessException ex)
    {
        return ex.getMessage();
    } 
}
