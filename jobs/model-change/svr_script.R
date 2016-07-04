require(cpm)
require(LiblineaR)
require(e1071)

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
train_test = function(d,start,end,test_points,perm=FALSE){
  if(perm!=FALSE)
    d = d[perm,]
  x = d[,1:100]
  y = d[,101] 
  mses = c()
  test_start = end-test_points+1
  params  = svr_params(x[start:(test_start-1),],y[start:(test_start-1)])
  mdl  = svm(x[start:(test_start-1),],y[start:(test_start-1)],type="eps-regression",kernel="linear",cost=params[[1]],epsilon=params[[2]])
  #mdl = lm(y~.,data=d[start:(test_start-1),])  
  ae = abs(y[test_start:end]-predict(mdl,x[test_start:end,]))
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
  x = d[,1:100]
  y = d[,101] 
  params0 = svr_params(x[1:t0,],y[1:t0])
  mdl0 = svm(x[1:t0,],y[1:t0],type="eps-regression",kernel="linear",cost=params0[[1]],epsilon=params0[[2]])
  pred0 = predict(mdl0,x[1:t0,])
  res = y[1:t0] - pred0
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
  cp = run_cpd(d[1:t0,101])
  
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
compare_methods = function(d,t0,min_train_size,test_size,ahead,window,thresh,type){
  
  min_size=min_train_size+test_size
  
  x = d[,1:100]
  y = d[,101] 
  plot(y)
  abline(v=t0)
  params0 = svr_params(x[1:t0,],y[1:t0])
  mdl0 = svm(x[1:t0,],y[1:t0],type="eps-regression",kernel="linear",cost=params0[[1]],epsilon=params0[[2]])
  pred0 = predict(mdl0,x[(t0+1):(t0+ahead),])
  er0 = mean(abs(y[(t0+1):(t0+ahead)] - pred0))
  
  #calls select_start to determine the starts of the training sets
  sel = select_start(d,t0,min_size,test_size,window,thresh,type)
  
  if(sel[[1]]==1)
    params1 = params0
  else
    params1  = svr_params(x[sel[[1]]:t0,],y[sel[[1]]:t0])
  mdl1  = svm(x[sel[[1]]:t0,],y[sel[[1]]:t0],type="eps-regression",kernel="linear",cost=params1[[1]],epsilon=params1[[2]])
  pred1 = predict(mdl1,x[(t0+1):(t0+ahead),])
  er1   = mean(abs(y[(t0+1):(t0+ahead)] - pred1))
  
  if(sel[[2]]==1)
    params2 = params0
  else
    params2 = svr_params(x[sel[[2]]:t0,],y[sel[[2]]:t0])
  mdl2 = svm(x[sel[[2]]:t0,],y[sel[[2]]:t0],type="eps-regression",kernel="linear",cost=params2[[1]],epsilon=params2[[2]])
  pred2 = predict(mdl2,x[(t0+1):(t0+ahead),])
  er2 = mean(abs(y[(t0+1):(t0+ahead)] - pred2))
  
  if(sel[[3]]==1)
    params3 = params0
  else
    params3 = svr_params(x[sel[[3]]:t0,],y[sel[[3]]:t0])
  mdl3 = svm(x[sel[[3]]:t0,],y[sel[[3]]:t0],type="eps-regression",kernel="linear",cost=params3[[1]],epsilon=params3[[2]])
  pred3 = predict(mdl3,x[(t0+1):(t0+ahead),])
  er3 = mean(abs(y[(t0+1):(t0+ahead)] - pred3))
  
  return(c(er0,er1,er3,er2))
  
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

svr_params = function(x,y){
  c = heuristicC(x)
  min_err = Inf
  min_e = 0.1
  #  for(e in 2**(-8:-6)){
  #    err = tryCatch(kfold_cv(x,y,c,e,5),error=function(err){
  #      return(Inf)
  #    })
  #    if(err<min_err){
  #      min_err=err
  #      min_e=e
  #    }
  #  }
  return(list(c,min_e))
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
