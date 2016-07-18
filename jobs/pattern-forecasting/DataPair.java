import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * This class stores the one water consumption value, a timestamp and optionally a value for the external temperature temperature
 * @author pant
 *
 */
public class DataPair {
	
	Calendar cal;
	String date;
	double consumption;
	double temperature;
	
	public DataPair(String date, double consumption, double temperature) throws ParseException{

		this.consumption = consumption;
		this.date=date;
		this.temperature=temperature;
		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
		cal = new GregorianCalendar();
		cal.setTime(sd.parse(date));
		
	}

	public DataPair(Calendar cal, double consumption){
		this.cal=cal;
		this.consumption=consumption;
		
	}
	
	public DataPair(String date, Calendar cal, double consumption){
		this.cal=cal;
		this.consumption=consumption;
		this.date=date;
		
	}
	
	public DataPair(double consumption){
		this.consumption = consumption;
	}
	
	public DataPair(Calendar cal, double consumption,double temperature){
		this.cal=cal;
		this.consumption=consumption;
		this.temperature = temperature;
		
	}

	public int getHour() {
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	public int getDay() {
		return cal.get(Calendar.DAY_OF_WEEK)-1;
	}

	public int getZone() {
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		
		if(hour>=1 && hour < 7)
			return 0;
		else if (hour>=7 && hour < 13 )
			return 1;
		else if (hour>=13 && hour < 19)
			return 2;
		else
			return 3;
	}

	public int getDate() {
		return cal.get(Calendar.DAY_OF_MONTH)-1;
	}
	
}
