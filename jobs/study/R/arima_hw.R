require(forecast)

arima_forall <- function(path){
  
  print("reading data")
  data = read.csv(path)
  vids = unique(data[1])
  
  mae = c();
  ns = c();
  count = 0;
  
  indices = c(168,5606,7008,8760)
  
  for (vid in vids[[1]]){
    
    print(vid)    
    ts = data[3][data[1]==vid]
  
    res  = try_arima(ts, indices[1],indices[3],indices[4]);
    mae = c(mae,res[[2]])
  
  }
  
  return(list(mae,ns))
  
}

try_arima <- function(y, trainStart, testStart , testEnd) {
  
  y_train <- y[trainStart:testStart-1]
  y_test  <- y[testStart:testEnd]
  
  y_pred <- rep(0,testEnd-testStart)
  mdl <- auto.arima(y_train)
  
  for (i in 0:(testEnd-testStart)){
    mdl <- Arima(y_train,model=mdl)
    y_pred[i+1] = as.numeric(forecast(mdl,h=1)$mean)
    y_train = c(y_train,y[testStart+i])
  }
  
  MAE <-  sum(abs(y_pred-y_test)/mean(y_test))/(length(y_pred))
  RMSE <-  sqrt(sum((y_pred-y_test)^2)/(testEnd-testStart))
  
  return(list(y_pred,MAE,RMSE,mdl))

}

try_hw<- function(y,trainStart,testStart,testEnd) {
  
  len     <- length(y)
  limit   <- testStart
  y_train <- y[trainStart:testStart]
  y_test  <- y[testStart+1:testEnd]
  
  mdl     <- HoltWinters(y_train,gamma=FALSE)
  y_pred  <- c()
  
  for (i in 0:(len-limit-1))
    y_pred = c (y_pred , as.numeric(forecast(HoltWinters(y[1:(limit+i)], alpha=mdl$alpha, beta=mdl$beta, gamma=mdl$gamma),1)$mean))
  
  
  NMAE <-  sum(abs(y_pred-y_test))/(len-limit)/mean(y_test)
  RMSE <-  sqrt(sum((y_pred-y_test)^2)/(len-limit))
  
  return(list(y_pred,NMAE,RMSE,res))
  
}

hw_forall <- function(path){
  
  data = read.csv(path)
  vids = unique(data[1])
  
  nmae = c();
  ns = c();
  count = 0;
  
  indices = c(168,5606,7008,8760)
  
  for (vid in vids[[1]]){
    
    print(vid)    
    ts = data[3][data[1]==vid]  
  
    res  = try_hw(ts,indices[1],indices[3],indices[4]);
   
    nmae = c(nmae,res[[2]])
    rmse = c(rmse,res[[3]])
    
    
  }
  
  return(list(nmae,rmse,ns))
  
}

all_silhouettes = function(k,path){
  data = read.csv(path)
  vids = unique(data$vid)
  silhs = c()
  for (vid in vids){
    ts = as.data.frame(data$volume[data$vid==vid])
    km = kmeans(ts, k)
    disim = daisy(ts)
    silhs = c(silhs, mean(silhouette(km$clus,disim)[,3]))
    
  }
  
  return(silhs)
  
}

all_ks = function(){
 
  sum = c()
  for(k in 2:12){
    sum = c(sum,mean(all_silhouettes(k)))
  }
  return(sum)
}

br_forall = function(start, end,path){
  data = read.csv(path)
  vids = unique(data[[1]])
  mapes = c()
  for(vid in vids){
    print(vid)
    ts = data[[3]][data[[1]]==vid][start:end]
    ts1 = ts[25:length(ts)]
    ts2 = ts[1:(length(ts)-24)]
    mapes = c(mapes, mean(abs(ts1-ts2)/mean(ts2)))
  }
  return(mapes)
}
