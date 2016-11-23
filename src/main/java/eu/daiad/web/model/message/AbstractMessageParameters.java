package eu.daiad.web.model.message;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.geotools.parameter.ParameterWriter;
import org.joda.time.DateTime;

import com.ibm.icu.text.DecimalFormat;

import eu.daiad.web.model.device.EnumDeviceType;

public abstract class AbstractMessageParameters implements IMessageParameters
{
    protected final DateTime refDate;
    
    protected final EnumDeviceType deviceType;

    protected static abstract class ParameterWrapper <P>
    {
        protected final P _value;
        
        ParameterWrapper(P p)
        {
            this._value = p;
        }
        
        public P value()
        {
            return _value;
        }
    }
    
    protected static class DateFormatter extends ParameterWrapper<DateTime>
    {
        private final String pattern;
        
        public DateFormatter(DateTime p)
        {
            super(p);
            this.pattern = "dd/MM/YYYY";
        }
        
        public DateFormatter(DateTime p, String pattern)
        {
            super(p);
            this.pattern = pattern;
        }
        
        @Override
        public String toString()
        {
            return _value.toString(pattern);
        }
    }
    
    protected static class NumberFormatter extends ParameterWrapper<Double>
    {
        private final String pattern;
        
        public NumberFormatter(Double p)
        {
            super(p);
            this.pattern = ".##";
        }
        
        public NumberFormatter(Double p, String pattern)
        {
            super(p);
            this.pattern = pattern;
        }
        
        @Override
        public String toString()
        {
            DecimalFormat formatter = new DecimalFormat(pattern);
            return formatter.format(_value);
        }
    }
    
    protected AbstractMessageParameters(DateTime refDate, EnumDeviceType deviceType)
    {
        this.refDate = refDate;
        this.deviceType = deviceType;
    }

    @Override
    public DateTime getReferenceDate()
    {
        return refDate;
    }

    @Override
    public EnumDeviceType getDeviceType()
    {
        return deviceType;
    }
    
    @Override
    public Map<String, Object> asParameters()
    {
        HashMap<String, Object> parameters = new HashMap<>(16);
        
        parameters.put("ref_date", new DateFormatter(refDate));
        parameters.put("device_type", deviceType);
        
        return parameters;
    }

}
