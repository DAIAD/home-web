require(neuralnet)

aNN24 = function(y,trainStart,testStart, testEnd){
  
  b=24

  m = max(y)
  y = y/m
  
  y_train = y[trainStart:testStart]
  y_test  = y[testStart:testEnd]
  
  dataset = matrix(ncol = b+1, nrow = testEnd-trainStart)
  for(i in trainStart:testEnd){
    for(j in 1:b)
      dataset[i-testStart+1,j+1] = y[i-j] 
    dataset[i-testStart,1] = y[i]
  }
  
  dataset = data.frame(dataset)
  
  ann = neuralnet(X1~X2+X3+X4+X5+X6+X7+X8+X9+X10+X11+X12+X13+X14+X15+X16+X17+X18+X19+X20+X21+X22+X23+X24+X25,dataset[1:(testStart-trainStart),],hidden=c(10,5),threshold=0.10)
  
  res = compute(ann,dataset[(testStart-trainStart):(testEnd-trainStart),2:25])$net.result
  print(mean(abs(y_test-res)/y_test))
  res=res*m
  y_test=y_test*m
  print(mean(abs(y_test-res)/y_test))
  
  return(list(res, y_test, mean(abs(y_test-res)/mean(y_test)) ,ann ))
      
}

aNN3 = function(y,ratio=0.7){
  b=3
  m = max(y)
  y = y/m
  len   = length(y)
  limit = round(len*ratio)
  y_train = y[1:limit]
  y_test  = y[(limit+1):len]
  dataset = matrix(ncol = b+1, nrow = len-b)
  for(i in (b+1):len){
    for(j in 1:b)
      dataset[i-b,j+1] = y[i-j] 
    dataset[i-b,1] = y[i]
  }
  
  dataset = data.frame(dataset)
  
  ann = neuralnet(X1~X2+X3+X4,dataset[1:(limit-b),],hidden=c(5,3,2),threshold=0.10)

  res = compute(ann,dataset[(limit+1-b):(len-b),2:4])$net.result
  res=res*m
  y_test=y_test*m
  
  return(list(res,y_test,mean(abs(y_test-res)/y_test)))
  
}

ann3_forall = function(path){
  
  data = read.csv(path)
  vids = unique(data$vid)
  vids = vids[vids!=4760]
  mape = c();
  rmse = c();
  ns  = c();
  count = 0;
  
  for (vid in vids){
    
    print(vid)    
    ts = data$volume[data$vid==vid]
    
    res  = aNN3(ts);
    
    mape = c(mape,res[[2]])
 #   write(res[[1]],paste('/home/pant/Desktop/f1/ann/',as.character(vid)),sep='\n')
  }
  
  return(list(mape))
  
}

ann24_forall <- function(path){
  
  data = read.csv(path)
  names = unique(data[[1]])
  indices = c(168,5606,7008,8760);
  mape = c();
  nmae = c();
  ns = c();
  count = 0;
  
  for (n in names){
    
    count = count + 1
    print(n)
    print(count)
    print('from')
    print(length(names))
  
    ts = data[[3]][data[[1]]==n]
    
    if(any(ts<0) || any(ts)>200 )
      next
    
    res = aNN24(ts,indices[1],indices[2],indices[3]);
  # mape = c(mape,res[[3]])
    nmae = c(nmae,res[[3]])
    ns = c(ns,n);
    
    print(ns)
    
  }
  
  return(list(nmae,ns))
  
}

