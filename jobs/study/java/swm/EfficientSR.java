import java.util.ArrayList;
import java.util.TreeMap;


public class EfficientSR implements Regressor {
	
	TreeMap<Integer[],Double[]> seqProbs;
	TreeMap<Integer[],Integer> seqCounts;
	double[] symMeans;
	double[] symCounts;
	double[][] PMF;
	double[][] CDF;
	double allMean;
	int symbols,b,type;
	boolean valid;
	
	
	public EfficientSR(int type){
		
		
		this.type = type;
		valid = true;
		
	}
	
	public void init(int k,int b,int trainEnd){
		seqProbs = new TreeMap<Integer[],Double[]>(new arrayComparator());
		seqCounts = new TreeMap<Integer[],Integer>(new arrayComparator());
		symMeans = new double[k];
		symCounts = new double[k];
		symbols = k;
		this.b = b;
		PMF = new double[symbols][trainEnd];
		CDF = new double[symbols][trainEnd];
		allMean=0;
	}
	
	public Integer[] makeIndex(ArrayList<Integer> seq, int cur){
		Integer[] index = new Integer[b];
		for(int i=b;i>0;i--)
			index[b-i] = seq.get(cur-i);
		return index;
		
	}
	
	public Double[] initDoubleArray(int size){
		Double[] buf = new Double[size];
		for(int i=0;i<size;i++)
			buf[i]=0.0;
		return buf;
	}
	
	public void incrementProb(Integer[] index,int sym){
		if (!seqProbs.containsKey(index))
			seqProbs.put(index, initDoubleArray(symbols));
		seqProbs.get(index)[sym]++;
		
	}
	
	public void incrementCount(Integer[] index){
		if (!seqCounts.containsKey(index))
			seqCounts.put(index, 0);
		seqCounts.put(index, seqCounts.get(index)+1);
	}
	
	public void normalizeProbs(){
		for(Integer[] ind: seqProbs.keySet()){
			Double[] buf = seqProbs.get(ind);
			double denom = seqCounts.get(ind);
			for(int i=0;i<symbols;i++)
				buf[i]/=denom;
		}
	}
	
	public double symbolProb(Integer[] index, Integer sym){
		
		if(seqProbs.containsKey(index))
			return seqProbs.get(index)[sym];
		else
			return 0;
	
	}
	
	public void train(AllData allData,double ratio,int k, int b){
	
		int trainEnd = (int)(ratio*(allData.data.size()-b));
		Integer[] index = new Integer[b];
		
		init(k,b,trainEnd);
		
		for(int i = b; i<trainEnd; i++){
			
		
			index = makeIndex(allData.discrete,i);
			incrementProb(index,allData.discrete.get(i));
			incrementCount(index);
			
			symMeans[allData.discrete.get(i)]+=allData.data.get(i);
			symCounts[allData.discrete.get(i)]++;
			
		}
		
		normalizeProbs();
		
		double num=0,denom=0;
		for(int i=0;i<symMeans.length;i++){
			symMeans[i]/=symCounts[i];
			num+=symMeans[i]*symCounts[i];
			denom+=symCounts[i];
		}
		
		allMean=num/denom;
		
		makeCDF(allData.discrete,trainEnd);	
		
	}
	
	public void makeCDF(ArrayList<Integer> sequence,int end){
		
		makePMF(sequence,end);
		for(int i=0;i<symbols;i++)
			for(int j=1;j<CDF[i].length;j++)
				CDF[i][j]=CDF[i][j-1]+PMF[i][j];
	
	}
	
	public void makePMF(ArrayList<Integer> sequence,int end){
		
		ArrayList<ArrayList<Integer>> intervals = countIntervals(sequence,end);
		ArrayList<Integer> ints;
		
		for(int i=0;i<symbols;i++){
			ints=intervals.get(i);
			for(Integer in:ints)
				PMF[i][in]+=1;
			for(int j=0;j<PMF[i].length;j++)
				PMF[i][j]/=ints.size();
		}
			
	}
	
	public ArrayList<ArrayList<Integer>> countIntervals(ArrayList<Integer> sequence,int end){
		
		ArrayList<ArrayList<Integer>> intervals = new ArrayList<ArrayList<Integer>>(symbols);
		int[] lastOcc = new int[symbols];

		for(int i=0;i<symbols;i++){
			lastOcc[i]=-1;
			intervals.add(new ArrayList<Integer>());
			
		}
		
		for(int i=0;i<end;i++){
			if (lastOcc[sequence.get(i)] == -1)
				lastOcc[sequence.get(i)] = i;
			else{
				intervals.get(sequence.get(i)).add(i-lastOcc[sequence.get(i)]);
				lastOcc[sequence.get(i)]=i;
			}
					
		}
		
		return intervals;
		
	}
	
	public int[] getPrev(ArrayList<Integer> sequence, int cur){
		
		int prev[] = new int[b];
		
		for(int i=0;i<b;i++)
			prev[i]=sequence.get(cur-b+i);
		
		return prev;
		
	}
	
	public int[] curInts(ArrayList<Integer> sequence,int cur){
		int[] lastSeen = new int[symbols];
		
		for(int i=0;i<cur;i++)
			if(i>lastSeen[sequence.get(i)])
				lastSeen[sequence.get(i)]=i;
		
		for(int i=0;i<symbols;i++)
			lastSeen[i]=cur-lastSeen[i];
		
		return lastSeen;
		
	}
	
	public double[] intProbs(int[] lastSeen){
		
		double[] probs = new double[symbols];
		
		for(int i=0;i<symbols;i++){
			if(lastSeen[i]>=CDF[i].length)
				lastSeen[i] = CDF[i].length-1;
			probs[i]=CDF[i][lastSeen[i]];
		}
		
		return probs;
		
	}
	
	public int predict(Integer[] index, int[] lastSeen,int type){
		int sym=-1;
		double max=-1;
		double[] intProbs = intProbs(lastSeen);
		
		
		for(int i=0;i<symbols;i++){
			double p1 = symbolProb(index,i);
			if(type==0){
				if(p1>max){
					max = p1;
					sym=i;
				}
			}
			if(type==1){
				if(intProbs[i]>max){
					max = intProbs[i];
					sym=i;
				}
			}
			if(type==2){
				if( (intProbs[i]+p1)/2>max){
					max = (intProbs[i]+p1)/2;
					sym=i;
				}
			}
			if(type==3){
				if(symCounts[i]>max){
					max=symCounts[i];
					sym=i;
				}
			}
		}

		
		return sym;
	}
	
	public Results test(AllData allData,double ratio,int b){
		
		int testStart = (int)((1-ratio)*(allData.discrete.size()-b));
		Integer[] index;
		int[] lastSeen;
		
		ArrayList<Integer> predictionsI = new ArrayList<Integer>();
		ArrayList<Double> predictionsD = new ArrayList<Double>();
		
		for(int i=b+testStart; i<allData.discrete.size();i++){
			index = makeIndex(allData.metaDiscrete,i);
			lastSeen = curInts(allData.discrete,i);
			predictionsI.add(predict(index,lastSeen,type));
		}
		
		for(Integer i:predictionsI){
			if(i!=-1)
				predictionsD.add(symMeans[i]);
			else
				predictionsD.add(0.0);
		}
	
		return new Results(predictionsD,predictionsI,testStart+b,symbols);
		
	}
	
	
}
