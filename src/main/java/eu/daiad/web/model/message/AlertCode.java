package eu.daiad.web.model.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

import eu.daiad.web.model.StringCode;

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

    public static AlertCode valueOf(String code)
    {
        Matcher m = pattern.matcher(code);
        Assert.isTrue(m.matches(), "Invalid alert code");
        return new AlertCode(code);
    }
}
