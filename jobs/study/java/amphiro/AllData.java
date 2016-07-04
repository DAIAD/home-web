import java.util.ArrayList;


public class AllData {
	
	public ArrayList<Double> data;
	public ArrayList<Integer> discrete;
	public ArrayList<MultiPoint> metadata;
	public ArrayList<Integer> metaDiscrete;
	public ArrayList<Range> ranges;
	
	public int discretization;
	
	public AllData(ArrayList<Double> data, ArrayList<Integer> discrete,ArrayList<MultiPoint> metadata,ArrayList<Integer> metaDiscrete,ArrayList<Range> ranges){
		this.data=data;
		this.discrete=discrete;
		this.metadata=metadata;
		this.metaDiscrete=metaDiscrete;
		this.ranges=ranges;
	}
	

	public void reDiscretize(boolean useMetadata, int discretization, int k, double ratio){
		
		ArrayList<Range> ranges;
		ArrayList<Double> trainData = new ArrayList<Double>(data.subList(0, (int)(ratio*data.size())));
		if(discretization==0)
			ranges = DataPreprocessor.clusterRanges(DataPreprocessor.cluster(trainData, k));
		else if( discretization == 1)
			ranges = DataPreprocessor.equalRanges(trainData, k);
		else
			ranges = DataPreprocessor.equiprobableRanges(trainData, k);
		discrete = DataPreprocessor.discretize(data, ranges);
		

		if ( useMetadata ){
	
			ArrayList<MultiPoint> trainMetadata = new ArrayList<MultiPoint> (metadata.subList(0,(int)(ratio*metadata.size())));
			try{
				metaDiscrete = DataPreprocessor.multiDiscretize(metadata, DataPreprocessor.multiCluster(trainMetadata, k));
			}
			catch(Exception e){
				metaDiscrete=discrete;
			}
			
		}
		else
			metaDiscrete = discrete;
		
	}
}
