require(cpm)
require(neuralnet)

#performs the change-point detection test for the baselines
run_cpd = function(x,ind=0){
  res = processStream(x,cpmType="Mann-Whitney")
  plot(x,xlab="day",ylab="error")
  abline(v=res$changePoints,col='red')
  abline(v=res$detectionTimes,col='red',lty=2)
  # title(ind)
  return(list(res$changePoints,res$detectionTimes,x))
}

#trains from 'start' to 'end' and measures error from 'end' to 'end'+'test_points'
test = function(d,start,end,test_points,perm=FALSE,w){
  if(perm!=FALSE)
    d = d[perm,]
  mses = c()
  test_start = end-test_points+1
  n = names(d)
  f = as.formula(paste('y ~', paste(n[!n %in% 'y'], collapse = ' + ')))
  mdl  = neuralnet(f,d[start:test_start,],hidden=5,threshold=0.01,rep=1)
  #mdl = lm(y~.,data=d[start:(test_start-1),])  
  res = compute(mdl,d[test_start:end,1:100])
  ae = abs(d[test_start:end,101]-res$net.result)
  return(ae)
}
#adds 1 point to the training set and measures the validation error, by calling train_test. (Algorithm 1)
seq_tt_d = function(d,t0,min_size,test_size,perm=FALSE){
  ers = c()
  points = seq(t0-min_size,1,-1)
  for(i in points){
    ers = c(ers, train_test(d,i,t0,test_size,perm))
  }
  return(list(t(matrix(ers,nrow=test_size)),points))
}

get_train_res = function(d,t0){
  n = names(d)
  f = as.formula(paste('y ~', paste(n[!n %in% 'y'], collapse = ' + ')))
  mdl  = neuralnet(f,data=d[1:t0,],hidden=5,threshold=0.01)
  #mdl = lm(y~.,data=d[start:(test_start-1),])  
  pred = compute(mdl,d[1:t0,1:100])$net.result
  res = d[1:t0,101] - pred
  return(res)
}

#selects the optimal point for the start of the training set (Algorithm 1)
select_start = function(d,t0,min_size=40,test_size=15,window=20,thresh=0.05,type="stat"){
  
  #seq_tt_d performs the iterating training-validating process of Algorithm 1
  ers = seq_tt_d(d,t0,min_size,test_size,FALSE)
  fin_er = ers[[1]][dim(ers[[1]])[1],]
  min_mid = min_moving_median(ers[[1]],window)
  min_mid_er = ers[[1]][min_mid[[1]],]
  
  #the condition that evaluates whether the decrease in the error is significan (Algorithm 2 line 6)
  if( type=="stat" && t.test(fin_er-min_mid_er)$p.value < thresh && mean(fin_er-min_mid_er)>0 ) 
    sel1 = ers[[2]][min_mid[[1]]]
  else if ( type=="emp" &&  mean(fin_er-min_mid_er)/mean(fin_er) > thresh )
    sel1 = ers[[2]][min_mid[[1]]]
  else
    sel1 = 1
  
  #runs change-point detection on the training residuals
  d1 = get_train_res(d,t0)
  cp = run_cpd(d1)
  
  if(length(cp[[1]])<1 )
    sel2 = 1
  else
    sel2 = cp[[1]][length(cp[[1]])]
  if(t0-sel2<20 && length(cp[[1]])>1)
    sel2 = cp[[1]][length(cp[[1]])-1]
  
  #runs change-point detection on the time series
  d2 = d[1:t0,101]
  cp = run_cpd(d2)
  
  if(length(cp[[1]])<1 )
    sel3 = 1
  else
    sel3 = cp[[1]][length(cp[[1]])]
  if(t0-sel3<20 && length(cp[[1]])>1)
    sel3 = cp[[1]][length(cp[[1]])-1]
  
  plot(ers[[2]],rowSums(ers[[1]])/test_size)
  abline(v=c(sel1,sel2,sel3),col=c(2,3,4))
  
  return(list(sel1,sel2,sel3))
  
}

#method that runs our algorithm and the baselines and measures the errors
compare_methods = function(d,t0,min_size,test_size,ahead,window,thresh,type="stat"){
  
  plot(d$y)
  abline(v=t0)
  
  y = d[,101]
  n = names(d)
  f = as.formula(paste('y ~', paste(n[!n %in% 'y'], collapse = ' + ')))
  s = max(d[1:t0,])
  d = d/s
  
  mdl0  = neuralnet(f,d[1:t0,],hidden=5,threshold=0.01)
  pred0 = compute(mdl0,d[(t0+1):(t0+ahead),1:100])$net.result*s
  er0   = mean(abs(y[(t0+1):(t0+ahead)] - pred0))
  
  #calls select_start to determine the starts of the training sets
  sel = select_start(d,t0,min_size,test_size,window,thresh,type)
  
  mdl1  = neuralnet(f,d[sel[[1]]:t0,],hidden=5,threshold=0.01)
  pred1 = compute(mdl1,d[(t0+1):(t0+ahead),1:100])$net.result*s
  
  if(sel[[1]]==1)
    er1=er0
  else
    er1   = mean(abs(y[(t0+1):(t0+ahead)] - pred1))
  
  mdl2  = neuralnet(f,d[sel[[2]]:t0,],hidden=5,threshold=0.01)
  pred2 = compute(mdl2,d[(t0+1):(t0+ahead),1:100])$net.result*s
  
  if(sel[[2]]==1)
    er2 = er0
  else
    er2 = mean(abs(y[(t0+1):(t0+ahead)] - pred2))
  
  mdl3  = neuralnet(f,d[sel[[3]]:t0,],hidden=5,threshold=0.01)
  pred3 = compute(mdl3,d[(t0+1):(t0+ahead),1:100])$net.result*s
  
  if(sel[[3]]==1)
    er3=er0
  else
    er3   = mean(abs(y[(t0+1):(t0+ahead)] - pred3))
  
  pred4 = mean(y[sel[[3]]:t0])
  er4 = mean(abs(y[(t0+1):(t0+ahead)] - pred4))
  
  return(c(er0,er1,er3,er2))
  
}


min_moving_median = function(matrix,w){
  min = Inf
  index = -1
  vector = rowSums(matrix)
  for(i in 1:(length(vector)-w)){
    med = median(vector[i:(i+w)])
    if(med<min){
      min = med
      index = i + (which(vector[i:(i+w)]==med))
    }
  }
  return(list(index,min))
}

#method that performs the test
test=function(data,min_train_size=10,val_size=25,points_ahead=10,window=10,thresh=0.1,type='stat'){
  ers = c()
  times = c()
  for(i in 1:length(synthetic_data)){
    print(i)
    d = data[[i]]
    for(t in c(80,120,160,200,240,280,320)){
      res = tryCatch(compare_methods(d,t,min_train_size,val_size,points_ahead,window,thresh,type),error=function(err){
        return(-1)
      })
      if (length(res)==1)
        next
      print(res)
      ers = c(ers, res)
      times = c(times , t)
    }
  }
  results = list(matrix(ers,nrow=4),times)
  print(parse_all(results))
  return(results)
}

parse_all = function(res){
  ers = matrix(0,3,8)
  times = c(80,120,160,200,240,280,320)
  for( i in 1:7){
    for(j in 1:3){
      r = res[[1]][,res[[2]]==times[i]]
      ers[j,i] = mean(r[1,]-r[(j+1),])/mean(r[1,])
    }
  }
  for(i in 1:3)
    ers[i,8] = mean(ers[i,1:7])
  
  return(ers*100)
}

