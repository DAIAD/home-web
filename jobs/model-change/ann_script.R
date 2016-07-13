require(cpm)
require(neuralnet)

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
  res = processStream(x,cpmType="Mann-Whitney")
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

train_test_24 = function(d,start,end,test_points,perm=FALSE,w){
  if(perm!=FALSE)
    d = d[perm,]
  mses = c()
  test_start = end-test_points+1
  n = names(d)
  f = as.formula(paste('y ~', paste(n[!n %in% 'y'], collapse = ' + ')))
  mdl  = neuralnet(f,d[start:test_start,],hidden=5,threshold=0.05,rep=1)
  #mdl = lm(y~.,data=d[start:(test_start-1),])  
  res = compute(mdl,d[test_start:end,1:31])
  ae = abs(d[test_start:end,32]-res$net.result)
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
  d = make_all_dataset_truth(user,h2)
  d = d/max(d)
  return(seq_tt_24_d(d,t0,min_size,test_size,perm))
}

get_train_res = function(user,h2,t0){
  d = make_all_dataset_truth(user,h2)
  n = names(d)
  f = as.formula(paste('y ~', paste(n[!n %in% 'y'], collapse = ' + ')))
  s = max(d[1:t0,])
  d = d/s
  mdl  = neuralnet(f,data=d[1:t0,],hidden=5,threshold=0.05,rep=1)
  #mdl = lm(y~.,data=d[start:(test_start-1),])  
  pred = compute(mdl,d[1:t0,1:31])$net.result*s
  res = d[1:t0,32] - pred
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
  if(t0-sel2<20 && length(cp[[1]])>1)
    sel2 = cp[[1]][length(cp[[1]])-1]
  
  d2 = make_all_dataset_truth(user)[1:t0,32]
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

compare_methods = function(user,h2,t0,min_size,test_size,ahead,window,thresh){
  
  d = make_all_dataset_truth(user,h2)
  plot(d$y)
  abline(v=t0)
  if(any(d$y>10000)||length(d$y)>10000)
    return(-d1)
  y = d$y
  n = names(d)
  f = as.formula(paste('y ~', paste(n[!n %in% 'y'], collapse = ' + ')))
  s = max(d[1:t0,])
  d = d/s
  
  mdl0  = neuralnet(f,d[1:t0,],hidden=5,threshold=0.05,rep=1)
  pred0 = compute(mdl0,d[(t0+1):(t0+ahead),1:31])$net.result*s
  er0   = mean(abs(y[(t0+1):(t0+ahead)] - pred0))
  
  sel = select_start_24(user,h2,t0,min_size,test_size,window,thresh)
  
  mdl1  = neuralnet(f,d[sel[[1]]:t0,],hidden=5,threshold=0.05,rep=1)
  pred1 = compute(mdl1,d[(t0+1):(t0+ahead),1:31])$net.result*s
  
  if(sel[[1]]==1)
    er1=er0
  else
    er1   = mean(abs(y[(t0+1):(t0+ahead)] - pred1))
  
  mdl2  = neuralnet(f,d[sel[[2]]:t0,],hidden=5,threshold=0.05,rep=1)
  pred2 = compute(mdl2,d[(t0+1):(t0+ahead),1:31])$net.result*s
  
  if(sel[[2]]==1)
    er2 = er0
  else
    er2 = mean(abs(y[(t0+1):(t0+ahead)] - pred2))
  
  mdl3  = neuralnet(f,d[sel[[3]]:t0,],hidden=5,threshold=0.05,rep=1)
  pred3 = compute(mdl3,d[(t0+1):(t0+ahead),1:31])$net.result*s
  
  if(sel[[3]]==1)
    er3=er0
  else
    er3   = mean(abs(y[(t0+1):(t0+ahead)] - pred3))
  
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
  ers = array(dim=c(4,8))
  t0 = c(80,120,160,200,240,280,320,340)
  for( j in t0  )
    ers[,which(t0==j)] = parse_results(res,j)
  return(ers)  
}


