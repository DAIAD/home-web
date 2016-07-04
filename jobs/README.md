# Algorithm Description

This folder contains the implementation for each algorithm developed as part of the [DAIAD](http://www.daiad.eu/) Project Deliverable 4.1.1

In the folder named `study` is the code for the initial study of water consumption, for Amphiro shower data and smart water meter data. There is a class for each tested algorithm, with a train and a test method that is used to interface with the algorithm. All algorithms are implemented in JAVA except ANN, Arima and Exponential Smoothing that are implemented in R.

In the folder named `pattern-forecasting` is the implementation of the Pattern Forecasting algorithm. It includes the code for analyzing the time series and providing the activity zones, the SVR array that provides the initial predictions and the Pattern Forecasting algorithm. The algorithm is implemented in JAVA.

In the folder named `model-change` is the implementation of the ITM algorithm. There is one script for each of the implemented models (Elastic-Net regularized linear regression, Support Vector Regression and Artificial Neural Network). Each script includes a select_start function that identifies the optimal starting point for the training of the model. The algorithms are implemented in R.

