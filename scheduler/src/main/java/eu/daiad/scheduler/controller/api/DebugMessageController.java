package eu.daiad.scheduler.controller.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.ValidationException;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.common.model.AuthenticatedRequest;
import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.message.AccountMessage;
import eu.daiad.common.model.message.Alert;
import eu.daiad.common.model.message.Announcement;
import eu.daiad.common.model.message.EnumMessageType;
import eu.daiad.common.model.message.Message;
import eu.daiad.common.model.message.MessageRequest;
import eu.daiad.common.model.message.MessageResult;
import eu.daiad.common.model.message.PerAccountMessages;
import eu.daiad.common.model.message.ReceiverAccount;
import eu.daiad.common.model.message.Recommendation;
import eu.daiad.common.model.message.Tip;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.repository.application.IUserRepository;
import eu.daiad.common.repository.application.IUtilityRepository;
import eu.daiad.common.service.message.IMessageService;
import eu.daiad.scheduler.controller.BaseController;
import eu.daiad.scheduler.util.csv.RecordMapper;
import eu.daiad.scheduler.util.csv.SimpleRecordMapper;

@RestController
public class DebugMessageController extends BaseController
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

    public static abstract class AccountMessagePrinter
    {
        public abstract String toLine(ReceiverAccount receiver, Message message) throws Exception;

        public abstract String toHeaderLine();
    }

    @SuppressWarnings("serial")
	public static class AccountTipPrinter extends AccountMessagePrinter
    {
        private static final RecordMapper<AccountMessage<Tip>> recordMapper;

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
            recordMapper = new SimpleRecordMapper<>(AccountMessage.class, fieldNames);
        }

        @Override
        public String toLine(ReceiverAccount receiver, Message message) throws Exception
        {
            return recordMapper.toLine(new AccountMessage<Tip>(receiver, (Tip) message));
        }

        @Override
        public String toHeaderLine()
        {
            return recordMapper.toHeaderLine();
        }
    }

    @SuppressWarnings("serial")
	public static class AccountAlertPrinter extends AccountMessagePrinter
    {
        private static final RecordMapper<AccountMessage<Alert>> recordMapper;

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
                put("message.deviceType", "Device");
                put("message.title","Title");
                put("message.description","Description");
                put("message.refDate","Reference-Date");
                put("message.createdOn","Created");
                put("message.acknowledgedOn","Acknowledged");
            }};
            recordMapper = new SimpleRecordMapper<>(AccountMessage.class, fieldNames);
        }

        @Override
        public String toLine(ReceiverAccount receiver, Message message) throws Exception
        {
            return recordMapper.toLine(new AccountMessage<Alert>(receiver, (Alert) message));
        }

        @Override
        public String toHeaderLine()
        {
            return recordMapper.toHeaderLine();
        }
    }

    @SuppressWarnings("serial")
    public static class AccountAnnouncementPrinter extends AccountMessagePrinter
    {
        private static final RecordMapper<AccountMessage<Announcement>> recordMapper;

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
            recordMapper = new SimpleRecordMapper<>(AccountMessage.class, fieldNames);
        }

        @Override
        public String toLine(ReceiverAccount receiver, Message message) throws Exception
        {
            return recordMapper.toLine(
                new AccountMessage<Announcement>(receiver, (Announcement) message));
        }

        @Override
        public String toHeaderLine()
        {
            return recordMapper.toHeaderLine();
        }
    }

    @SuppressWarnings("serial")
    public static class AccountRecommendationPrinter extends AccountMessagePrinter
    {
        private static final RecordMapper<AccountMessage<Recommendation>> recordMapper;

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
                put("message.deviceType", "Device");
                put("message.title","Title");
                put("message.description","Description");
                put("message.refDate","Reference-Date");
                put("message.createdOn","Created");
                put("message.acknowledgedOn","Acknowledged");
            }};
            recordMapper = new SimpleRecordMapper<>(AccountMessage.class, fieldNames);
        }

        @Override
        public String toLine(ReceiverAccount receiver, Message message) throws Exception
        {
            return recordMapper.toLine(
                new AccountMessage<Recommendation>(receiver, (Recommendation) message));
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

        public ExportResponse(eu.daiad.common.model.error.Error e)
        {
            super(e);
        }

        @JsonProperty
        private List<PerAccountMessages> accounts = new ArrayList<>();
    }

    @PostMapping(
        value = "/api/v1/message/export",
        consumes = "application/json",
        produces = "application/json"
    )
    public RestResponse exportMessages(@RequestBody ExportRequest request)
    {
        try {
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage());
            return new RestResponse(getError(ex));
        }

        MessageRequest r = new MessageRequest(request.getLocale())
            .withOptions(new MessageRequest.Options(request.getType(), request.getLimit()));

        ExportResponse response = new ExportResponse();
        try {
            for (UUID accountKey: utilityRepository.getMembers(request.getUtility())) {
                AuthenticatedUser user = userRepository.getUserByKey(accountKey);
                MessageResult q = service.getMessages(user, r);
                if (!q.getMessages().isEmpty()) {
                    ReceiverAccount receiver = ReceiverAccount.of(accountKey, user.getUsername(), user.getFirstname(), user.getLastname());
                    response.accounts.add(new PerAccountMessages(receiver, q.getMessages()));
                }
            }
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);
            response = new ExportResponse(getError(ex));
        }

        return response;
    }

    @PostMapping(
        value = "/api/v1/message/export.csv",
        consumes = "application/json",
        produces = "text/csv"
    )
    public String exportMessagesToSpreadsheet(@RequestBody ExportRequest request)
        throws IllegalAccessException
    {
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
            ReceiverAccount receiver = ReceiverAccount.of(accountKey, user.getUsername(), user.getFirstname(), user.getLastname());
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
