package eu.daiad.common.model.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;

import eu.daiad.common.model.StringCode;

/**
 * Represent a code for an alert message.
 * 
 * The full list of defined codes can be found at DAIAD deliverable 3.2.2. (Annex/Messages)
 */
public class AlertCode extends StringCode
{
    private static final Pattern pattern = Pattern.compile("^[AP][1-9][0-9]*$");
    
    protected AlertCode(String code)
    {
        super(code);
    }

    @JsonCreator
    public static AlertCode valueOf(String code)
    {
        Matcher m = pattern.matcher(code);
        Assert.isTrue(m.matches(), "Invalid alert code");
        return new AlertCode(code);
    }
}
