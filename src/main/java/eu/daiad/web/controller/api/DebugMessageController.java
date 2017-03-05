package eu.daiad.web.controller.api;

import java.io.OutputStream;
import java.io.PrintWriter;
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
import eu.daiad.web.model.message.AlertCode;
import eu.daiad.web.model.message.EnumMessageType;
import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.MessageRequest;
import eu.daiad.web.model.message.MessageResult;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.Credentials;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.security.RoleConstant;
import eu.daiad.web.model.user.UserInfo;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IUtilityRepository;
import eu.daiad.web.service.message.IMessageService;

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
    
    private static class ExportRequest extends AuthenticatedRequest
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
    
    private static class MessagesPerAccount 
    {
        @JsonProperty
        private final UUID key;
        
        @JsonProperty
        private final String username;
        
        @JsonProperty
        private List<Message> messages;

        public MessagesPerAccount(UUID key, String username, List<Message> messages)
        {
            this.key = key;
            this.username = username;
            this.messages = messages;
        }
    }
    
    private static class ExportResponse extends RestResponse
    {
        public ExportResponse() {}
        
        public ExportResponse(eu.daiad.web.model.error.Error e) 
        {
            super(e);
        }
        
        @JsonProperty
        private List<MessagesPerAccount> accounts = new ArrayList<>();
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
                    response.accounts.add(
                        new MessagesPerAccount(accountKey, user.getUsername(), q.getMessages()));
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
       
        MessageRequest r = new MessageRequest(request.getLocale())
            .withOptions(new MessageRequest.Options(request.getType(), request.getLimit()));
        
        final String[] userRowHeaders = new String[] {"User-Name", "User-Key"};
        final String separator = ";";
        
        StringBuilder outs = new StringBuilder();
        boolean writeHeaderRow = true;
        for (UUID accountKey: utilityRepository.getMembers(request.getUtility())) {
            AuthenticatedUser user = userRepository.getUserByKey(accountKey);
            MessageResult q = service.getMessages(user, r);
            List<Message> messages = q.getMessages();
            if (messages.isEmpty())
                continue; // skip; no messages for this user
            Object[] userRowData = new Object[] { user.getUsername(), user.getKey() };
            for (Message message: messages) {
                if (writeHeaderRow) {
                    Object[] headerRow = ArrayUtils.addAll(userRowHeaders, message.toRowHeaders());
                    outs.append(StringUtils.join(headerRow, separator) + "\n\n");
                    writeHeaderRow = false;
                }
                Object[] dataRow = ArrayUtils.addAll(userRowData, message.toRowData());
                outs.append(StringUtils.join(dataRow, separator) + "\n");
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
