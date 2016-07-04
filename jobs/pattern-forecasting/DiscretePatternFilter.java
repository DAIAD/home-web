import java.util.ArrayList;
import java.util.Calendar;

/**
 * Class that combines the predictions from DiscretePatternForecaster and Linear24hSVR 
 * @author pant
 *
 */
public class DiscretePatternFilter {

	/**
	 * Function that combines the predictions from DiscretePatternForecaster and Linear24hSVR. It applies the prediction 
	 * for the activity zones made by DiscretePatternForecaster to the time series prediction made by Linear24hSVR
	 * @param dpf A Results object containing predictions from DiscretePatternForecaster 
	 * @param svr A Results object containing predictions from Linear24hSVR
	 * @return A Results object containing the modified predictions
	 */
	public Results filter(Results dpf, Results svr){
		
		Results res = svr.clone();
		
		ArrayList<ActivityZone> zones = res.zones;
		
		for(int i=0; i<res.predDRR.size();i++)
			
			for(int z=0;z<zones.size();z++)	
				
				if( svr.predDRR.get(i).zones.get(z).value == 0 &&  dpf.predDRR.get(i).zones.get(z).value == 1 )
					res.predDRR.get(i).zones.get(z).value = 1;
				
				else if( svr.predDRR.get(i).zones.get(z).value == 1 && dpf.predDRR.get(i).zones.get(z).value == 0){
					removePattern(res.predTS.get(i),zones.get(z));
					
					res.predDRR.get(i).zones.get(z).value = 0;
				}
				
		res.calcAllErrors();
		
		return res;
		
	}

	private void removePattern(double[] ds, ActivityZone z) {
		
		for(int i=z.aggStart;i<z.aggEnd;i++)	
			ds[i]=0;
	
	}
	
	public double[] filter(DayReducedRepresentation dpf, double[] svr,ArrayList<ActivityZone> zones){
		
		ArrayList<DataPair> day = new ArrayList<DataPair>();
		
		for(int i=0;i<24;i++)
			day.add( new DataPair( null, svr[i+1] ));
		
		DayReducedRepresentation drrSvr = (new Day(day,null)).aggregateDay(zones);
		
		for(int z=0;z<zones.size();z++)	
			if( drrSvr.zones.get(z).value == 1 &&  dpf.zones.get(z).value == 0)
				removePattern(svr,zones.get(z));

		return svr;
	}
	
}
