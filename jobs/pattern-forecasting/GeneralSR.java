import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.TreeMap;


public class GeneralSR {//implements Regressor{
	
	TreeMap<Integer[],Integer[]> counts;
	int[][][] independentCounts;
	Integer[] symCounts;
	int symbols;
	
	public GeneralSR(int symbols){
		counts = new TreeMap<Integer[],Integer[]>(new arrayComparator());
		symCounts = new Integer[symbols];
		for(int i=0;i<symbols;i++)
			symCounts[i]=0;
		this.symbols = symbols;
	}
	
	public void updateCounts(Integer[] key,Integer val){
		if (!counts.containsKey(key))
			addKey(key);
		counts.get(key)[val]++;
		symCounts[val]++;
	}
	
	public void train(ArrayList<Integer[]> keys, ArrayList<Integer> values) throws FileNotFoundException{
	
		initIndipendentCounts(keys.get(0).length);
		Integer[] key;
		Integer   val;
				
		for(int i=0;i<keys.size();i++){
			
			key = keys.get(i);
			val = values.get(i);
			
			updateCounts(key,val);
			updateIndependentCounts(key,val);
			
		}
		
	}
			
	private void updateIndependentCounts(Integer[] key, Integer val) {
		
		for(int z=0;z<key.length;z++)
			independentCounts[z][key[z]][val]++;
		
	}

	public void initIndipendentCounts(int zones) {
		
		independentCounts = new int[zones][symbols][symbols];
		
		for(int z=0;z<zones;z++)
			for(int x=0;x<symbols;x++)
				for(int y=0;y<symbols;y++)
					independentCounts[z][x][y]++;
		
		for(int i=0;i<symbols;i++)
			symCounts[i]=1;
		
	}

	private void addKey(Integer[] key) {
		Integer[] vals = new Integer[symbols];
		for(int i=0;i<symbols;i++)
			vals[i]=new Integer(0);
		counts.put(key, vals);
	}
	
	public Integer predict(Integer[] key){
		Integer[] buf;
	
		if(counts.containsKey(key))
			buf = counts.get(key);
		else
			buf=symCounts;
		
		int maxCount = -1;
		int maxSym = -1;
		for(Integer i=0;i<symbols;i++)
			if(buf[i]>maxCount){
				maxCount = buf[i];
				maxSym = i;
			}
		return maxSym;
	
	}
	
	public double singleSymbolCounts(Integer[] x, Integer y){
		double prod = 1;
		for(int z=0;z<x.length;z++)
			prod *= independentCounts[z][x[z]][y];
		prod /= Math.pow(symCounts[y],x.length-1);
		return prod;
	}
	
	public Integer independentPredict(Integer[] x){
		
		double max=-1,buf;
		Integer yMax=-1;
		
		for(int y=0;y<symbols;y++){
			buf = singleSymbolCounts(x,y);
			if(buf>max){
				max=buf;
				yMax=y;
			}
		}
		
		return yMax;
	
	}
		
}

	
	
