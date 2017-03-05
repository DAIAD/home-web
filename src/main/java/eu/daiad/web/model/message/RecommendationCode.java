package eu.daiad.web.model.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;

import eu.daiad.web.model.StringCode;

/**
 * Represent a code for a recommendation message.
 * 
 * The full list of defined codes can be found at DAIAD deliverable 3.2.2. (Annex/Messages)
 */
public class RecommendationCode extends StringCode
{
    private static final Pattern pattern = Pattern.compile("^(R|(I[ABC]))[1-9][0-9]*$");
    
    protected RecommendationCode(String code)
    {
        super(code);
    }

    @JsonCreator
    public static RecommendationCode valueOf(String code)
    {
        Matcher m = pattern.matcher(code);
        Assert.isTrue(m.matches(), "Invalid recommendation code");
        return new RecommendationCode(code);
    }
}
