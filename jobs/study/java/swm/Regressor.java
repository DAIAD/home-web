
public interface Regressor {
	
	public void train(AllData allData,double ratio,int k,int b);
	
	public Results test(AllData allData,double ratio,int b);		

}
