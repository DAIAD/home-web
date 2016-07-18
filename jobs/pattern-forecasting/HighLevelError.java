import java.util.ArrayList;


public class HighLevelError {

	class ConfusionMatrix{
		double TP,FP,TN,FN;
		double ACC;
	}
	
	ConfusionMatrix[] cms;
	int symbols;
	
	double TP,FP,TN,FN;
	double meanACC;
	double p00,p11;
	
	double consMAPE;
	double consMAE;
	
	double timeMAE;
	
	public HighLevelError(ArrayList<Day> days, ArrayList<DayReducedRepresentation> predDRR,ArrayList<ActivityZone> zones){
		
		if(zones.size()==0)
			symbols=0;
		else
			symbols = zones.get(0).thresholds.size();
		
		cms = new ConfusionMatrix[symbols];
		
		for(int s=0;s<symbols;s++)
			cms[s] = new ConfusionMatrix();
		
		consMAPE=0;
		consMAE=0;
		timeMAE=0;
		
		calcHLE(days,predDRR,zones);
	
	}
	
	void calcHLE(ArrayList<Day> days, ArrayList<DayReducedRepresentation> predDRR,ArrayList<ActivityZone> zones){
		
		for(int i=0;i<days.size();i++)
			HLEofDay(days.get(i),predDRR.get(i),zones);
		finalize();
		
	}
	
	void classErrors(double truth, double pred){
		
		for(int s=0;s<symbols;s++)
			if(truth!=s && pred!=s)
				cms[s].TN++;
			else if(truth!=s && pred==s)
				cms[s].FP++;
			else if(truth==s && pred!=s)
				cms[s].FN++;
			else if(truth==s && pred==s)
				cms[s].TP++;
	
	}
	
	void HLEofDay(Day d, DayReducedRepresentation pred,ArrayList<ActivityZone> zones){
		
		DayReducedRepresentation base = d.aggregateDay(zones);
		
		for(int i=0;i<zones.size();i++){
			classErrors(base.zones.get(i).value,pred.zones.get(i).value);
		/*	if(base.zones.get(i).value==1 && pred.zones.get(i).value==1){
				consMAE  += Math.abs(base.volumes.get(i).value - pred.volumes.get(i).value);
				consMAPE += Math.abs(base.volumes.get(i).value - pred.volumes.get(i).value)/base.volumes.get(i).value;
				timeMAE  += Math.abs(base.times.get(i).value - pred.times.get(i).value);
			}
			if(base.zones.get(i).value==1 && pred.zones.get(i).value==0)
				consMAE  += Math.abs(base.volumes.get(i).value - 0);
			if(base.zones.get(i).value==0 && pred.zones.get(i).value==1)
				consMAE  += Math.abs(pred.volumes.get(i).value - 0);
		*/
		}	
	
	}
	
	public void finalize(){
		consMAPE/=TP;
		consMAE/=TP;
		timeMAE/=TP;
	
		double sum=0;
		
		for(int s=0;s<symbols;s++){
			cms[s].ACC = (cms[s].TP+cms[s].TN)/(cms[s].TP+cms[s].TN+cms[s].FP+cms[s].FN);
			sum+=cms[s].ACC;
		}
		
		meanACC = sum/symbols;
	
	}

}
