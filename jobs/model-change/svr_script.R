require(cpm)
require(LiblineaR)
require(e1071)

get_data = function(path){
  f     = file(path,open="r")
  lines = readLines(f) 
  close(f)
  data = list()
  for(i in 1:(length(lines)/3)){
    index = (i-1)*3
    data[[i]] = list()
    data[[i]]$id     = strsplit(lines[index+1]," ")[[1]][1]
    data[[i]]$cons   = as.numeric(strsplit(lines[index+2]," ")[[1]])
    data[[i]]$dates  = as.Date(strsplit(lines[index+3]," ")[[1]])
  }
  return(data)
}

make_all_dataset_truth = function(user,hour,end=360){
  x = t(matrix(user$cons[1:(end*24)],nrow=24))
  d = make_dates(as.factor(weekdays(user$dates)[1:end]))
  y = rowSums(t(matrix(user$cons[25:((end+1)*24)],nrow=24)))
  f = data.frame(cbind(x,d,y))
  #f= cbind(x,y)
  colnames(f) = c(make.names(1:31),"y")
  return(f)
}

make_dates = function(dates){
  date_matrix = matrix(0,length(dates),7)
  for(i in 1:length(dates))
    date_matrix[i,dates[i]]=1
  return(date_matrix)
}

run_cpd = function(x,ind=0){
  res = processStream(x,cpmType="Mann-Whitney",ARL0=200)
  plot(x,xlab="day",ylab="error")
  abline(v=res$changePoints,col='red')
  abline(v=res$detectionTimes,col='red',lty=2)
  # title(ind)
  return(list(res$changePoints,res$detectionTimes,x))
}

monthly_filter = function(user){
  len = floor(length(user$cons)/24/30)*30*24
  if(len>10000)
    return(FALSE)
  d = t(matrix(user$cons[1:len],nrow=24*30))
  month_cons = rowSums(d)
  # plot(month_cons)
  if(sum(sort(month_cons,decreasing=TRUE)[1:6])>0.8*sum(month_cons))
    return(FALSE)
  else
    return(TRUE)
}

train_test_24 = function(d,start,end,test_points,perm=FALSE){
  if(perm!=FALSE)
    d = d[perm,]
  x = d[,1:31]
  y = d[,32] 
  mses = c()
  test_start = end-test_points+1
  params  = svr_params(x[start:(test_start-1),],y[start:(test_start-1)])
  mdl  = svm(x[start:(test_start-1),],y[start:(test_start-1)],type="eps-regression",kernel="linear",cost=params[[1]],epsilon=params[[2]])
  #mdl = lm(y~.,data=d[start:(test_start-1),])  
  ae = abs(y[test_start:end]-predict(mdl,x[test_start:end,]))
  return(ae)
}

seq_tt_24_d = function(d,t0,min_size,test_size,perm=FALSE){
  ers = c()
  points = seq(t0-min_size,1,-1)
  for(i in points){
    ers = c(ers, train_test_24(d,i,t0,test_size,perm))
  }
  return(list(t(matrix(ers,nrow=test_size)),points))
}
seq_tt_24 = function(user,h2,t0,min_size,test_size,perm=FALSE){
  d = data.matrix(make_all_dataset_truth(user,h2))
  return(seq_tt_24_d(d,t0,min_size,test_size,perm))
}

get_train_res = function(user,h2,t0){
  d = data.matrix(make_all_dataset_truth(user,h2))
  x = d[,1:31]
  y = d[,32] 
  params0 = svr_params(x[1:t0,],y[1:t0])
  mdl0 = svm(x[1:t0,],y[1:t0],type="eps-regression",kernel="linear",cost=params0[[1]],epsilon=params0[[2]])
  pred0 = predict(mdl0,x[1:t0,])
  res = y[1:t0] - pred0
  return(res)
}

select_start_24 = function(user,h2,t0,min_size=40,test_size=15,window=20,thresh=0.05){
  
  ers = seq_tt_24(user,h2,t0,min_size,test_size,FALSE)
  fin_er = ers[[1]][dim(ers[[1]])[1],]
  min_mid = min_moving_median(ers[[1]],window)
  min_mid_er = ers[[1]][min_mid[[1]],]
  
  if( t.test(fin_er-min_mid_er)$p.value<thresh && mean(fin_er-min_mid_er)>0) 
    sel1 = ers[[2]][min_mid[[1]]]
  else
    sel1 = 1
  
  d1 = get_train_res(user,h2,t0)
  cp = run_cpd(d1)
  
  if(length(cp[[1]])<1 )
    sel2 = 1
  else
    sel2 = cp[[1]][length(cp[[1]])]
  if(t0-sel2<50)
    sel2 = 1
  
  d2 = make_all_dataset_truth(user)[1:t0,32]
  cp = run_cpd(d2)
  
  if(length(cp[[1]])<1 )
    sel3 = 1
  else
    sel3 = cp[[1]][length(cp[[1]])]
  if(t0-sel3<50)
    sel3 = 1
  
  plot(ers[[2]],rowSums(ers[[1]])/test_size)
  abline(v=c(sel1,sel2,sel3),col=c(2,3,4))
  
  
  return(list(sel1,sel2,sel3))
  
}

compare_methods = function(user,h2,t0,min_size,test_size,ahead,window,thresh){
  
  d = data.matrix(make_all_dataset_truth(user,h2))
  x = d[,1:31]
  y = d[,32] 
  plot(y)
  abline(v=t0)
  if(any(y>10000)||length(y)>10000)
    return(-1)
  params0 = svr_params(x[1:t0,],y[1:t0])
  mdl0 = svm(x[1:t0,],y[1:t0],type="eps-regression",kernel="linear",cost=params0[[1]],epsilon=params0[[2]])
  pred0 = predict(mdl0,x[(t0+1):(t0+ahead),])
  er0 = mean(abs(y[(t0+1):(t0+ahead)] - pred0))
  
  sel = select_start_24(user,h2,t0,min_size,test_size,window,thresh)
  
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
  
  pred4 = mean(y[sel[[3]]:t0])
  er4 = mean(abs(y[(t0+1):(t0+ahead)] - pred4))
  
  return(c(er0,er1,er2,er3,er4))
  
}

compare_many = function(start,end,min_size,test_size,ahead,window,thresh,path){
  tried = c()
  ers   = c()
  users = get_data(path)
  for(u in start:end){
    if(monthly_filter(users[[u]]))
      for( t in c(80,120,160,190,200,230,240,230,270,280,310,320,340)){
        
        part = c()
        #h = sample(6:24,1)
        
        print(paste(u,t,sep=" "))
        res = tryCatch(compare_methods(users[[u]],h,t,min_size,test_size,ahead,window,thresh),error=function(err){
          print(err)
          return(-1)
        })
        
        if(res==-1)
          next
        
        tried = c(tried,u,t)
        ers   = c(ers,res)
        print(res)
        
      }
  }
  
  res = list(matrix(ers,nrow=5),matrix(tried,nrow=2))
  save(res,file=paste("res-",min_size,test_size,ahead,window,thresh,".RData",sep=""))
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

parse_results = function(res,t0){
  hours = c()
  ids = c()
  for(i in 1:dim(res[[1]])[2]){
    if(res[[2]][2,i]==t0){
      hours = c(hours,res[[1]][,i])
      ids = c(ids,res[[2]][,i] )
    }
  }
  ers = matrix(hours,nrow=5)
  ids = matrix(ids, nrow=2)
  r = c()
  r = c(r,mean((ers[1,]-ers[2,])/ers[1,]))
  r = c(r,mean((ers[1,]-ers[3,])/ers[1,]))
  r = c(r,mean((ers[1,]-ers[4,])/ers[1,]))
  r = c(r,mean((ers[1,]-ers[5,])/ers[1,]))
  
  return(r)
}

parse_all = function(res){
  ers = array(dim=c(4,4))
  t0 = c(200,240,280,320)
  for( j in t0  )
    ers[,which(t0==j)] = parse_results(res,j)
  return(ers)  
}

kfold_cv = function(x,y,c,e,k=5){
  len       = length(y)
  fold_size =  floor(len/k)
  mse = 0
  for(i in 0:(k-1)){
    test_ind = (i*fold_size+1):(i*fold_size+fold_size)
    train_x  = x[-test_ind,]
    train_y = y[-test_ind]
    test_x = x[test_ind,]
    test_y = y[test_ind]
    mdl = svm(train_x,train_y,kernel="linear",type="eps-regression",cost=c,epsilon=e)
    pred = predict(mdl,test_x)
    buf = mean((pred-test_y)**2)
    mse = mse+buf
  }
  return(mse/k)
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

