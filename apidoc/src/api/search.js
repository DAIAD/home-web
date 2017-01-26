/**
 * @api {post} /v1/meter/status Meter current status
 * @apiVersion 0.0.1
 * @apiName MeterStatus
 * @apiGroup Search
 * @apiPermission ROLE_USER
 * 
 * @apiDescription Loads the most recent status of one or more meter devices.
 *
 * @apiParam (DeviceRegistrationRequest) {Object}     credentials                 User credentials
 * @apiParam (DeviceRegistrationRequest) {String}     credentials.username        User name.
 * @apiParam (DeviceRegistrationRequest) {String}     credentials.password        Password.
 * @apiParam (DeviceRegistrationRequest) {String[]}   deviceKey                   Array of unique identifiers (UUID) for authenticated user meter devices.
 * 
 * @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     "username":"user@daiad.eu",
 *     "password":"****",
 *   },
 *   "deviceKey": [
 *     "114e7af3-39af-4a44-ac57-41779689ec39"
 *   ]
 * }
 * 
 * @apiSuccess (WaterMeterStatusQueryResult) {Boolean}  success             Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess (WaterMeterStatusQueryResult) {Object[]} errors              Array of <code>Error</code>.
 * @apiSuccess (WaterMeterStatusQueryResult) {Object[]} devices             Array of <code>WaterMeterStatus</code>.
 * 
 * @apiSuccess (WaterMeterStatus) {String} deviceKey          Device unique key (UUID).
 * @apiSuccess (WaterMeterStatus) {String} serial             Meter unique serial number.
 * @apiSuccess (WaterMeterStatus) {Number} timestamp          Last reading time stamp.
 * @apiSuccess (WaterMeterStatus) {Number} volume             Current volume measurement.
 * @apiSuccess (WaterMeterStatus) {Number} variation          Difference between the last two readings.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "devices": [{
 *     "deviceKey": "114e7af3-39af-4a44-ac57-41779689ec39",
 *     "serial": "I33DA124580",
 *     "timestamp": 1463051902000,
 *     "volume": 243755,
 *     "variation": 0
 *   }],
 *   "success": true
 * }
 * 
 * @apiError {Boolean} success Always <code>false</code>.
 * @apiError {Object[]} errors Array of <code>Error</code> objects.
 * 
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 * 
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "UserErrorCode.USER_NOT_FOUND",
 *     description: "Account a9509da9-edf5-4838-acf4-8f1b73485d7a was not found."
 *   }],
 *   success: false
 * }
 * 
 */
function meterStatus() { return; }

/**
 * @api {post} /v1/meter/history Search meter measurements
 * @apiVersion 0.0.1
 * @apiName MeterMeasurements
 * @apiGroup Search
 * @apiPermission ROLE_USER
 * 
 * @apiDescription Loads measurements for one or more meter devices.
 *
 * @apiParam (WaterMeterMeasurementQuery) {Object}     credentials                 User credentials
 * @apiParam (WaterMeterMeasurementQuery) {String}     credentials.username        User name.
 * @apiParam (WaterMeterMeasurementQuery) {String}     credentials.password        Password.
 * @apiParam (WaterMeterMeasurementQuery) {String[]}   deviceKey                   Array of unique identifiers (UUID) for authenticated user meter devices.
 * @apiParam (WaterMeterMeasurementQuery) {Number}     granularity                 Sets the data aggregation level. Valid values are:<br/><br/>
 * 0: <code>NONE</code><br/>
 * 1: <code>HOUR</code><br/>
 * 2: <code>DAY</code><br/>
 * 3: <code>WEEK</code><br/>
 * 4: <code>MONTH</code><br/>
 * 5: <code>YEAR</code><br/>
 * @apiParam (WaterMeterMeasurementQuery) {Number}     startDate                   Time interval start time stamp.
 * @apiParam (WaterMeterMeasurementQuery) {Number}     endDate                     Time interval end time stamp.
 * 
 * @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     "username":"user@daiad.eu",
 *     "password":"****",
 *   },
 *   "deviceKey": [
 *     "114e7af3-39af-4a44-ac57-41779689ec39"
 *   ],
 *   "granularity": 3,
 *   "startDate":1460926800000,
 *   "endDate":1461531599000
 * }
 * 
 * @apiSuccess (WaterMeterMeasurementQueryResult) {Boolean}  success            Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess (WaterMeterMeasurementQueryResult) {Object[]} errors             Array of <code>Error</code>.
 * @apiSuccess (WaterMeterMeasurementQueryResult) {Object[]} series             Array of <code>WaterMeterDataSeries</code>.
 * 
 * @apiSuccess (WaterMeterDataSeries) {String}               deviceKey          Device unique key (UUID).
 * @apiSuccess (WaterMeterDataSeries) {String}               serial             Meter unique serial number.
 * @apiSuccess (WaterMeterDataSeries) {Object}               reference          Reference data point. That is an instance of <code>WaterMeterDataPoint</code> with the most recent data point to the start of the query interval. This property has been used for computing meter variations but now is obsolete since the response always returns the variation in property <code>difference</code> of <code>WaterMeterDataPoint</code>.
 * @apiSuccess (WaterMeterDataSeries) {OBject[]}             values             Array of <code>WaterMeterDataPoint</code>.
 * @apiSuccess (WaterMeterDataSeries) {Number}               minTimestamp       Minimum time stamp for returned data points.
 * @apiSuccess (WaterMeterDataSeries) {Number}               maxTimestamp       Maximum time stamp for returned data points.
 * 
 * @apiSuccess (WaterMeterDataPoint)  {Number}               timestamp          Data point timestamp.
 * @apiSuccess (WaterMeterDataPoint)  {Number}               volume             Volume.
 * @apiSuccess (WaterMeterDataPoint)  {Number}               difference         Difference from previous reading.
 * @apiSuccess (WaterMeterDataPoint)  {Number}               week               Week of year for the current data point.
 * 
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "series": [{
 *     "deviceKey": "48944e9f-471d-4a9a-a926-f23dee64ae6a",
 *     "serial": "I33DA124580",
 *     "reference": {
 *       "timestamp": 1460844000000,
 *       "volume": 900140,
 *       "difference": 0,
 *       "week": 15
 *     },
 *     "values": [{
 *       "timestamp": 1460844000000,
 *       "volume": 900140,
 *       "difference": 1016,
 *       "week": 15
 *     }, {
 *       "timestamp": 1461448800000,
 *       "volume": 904114,
 *       "difference": 327,
 *       "week": 16
 *     }],
 *     "minTimestamp": 1460332800000,
 *     "maxTimestamp": 1461542399000
 *   }],
 *   "success": true
 * }
 * 
 * @apiError {Boolean} success Always <code>false</code>.
 * @apiError {Object[]} errors Array of <code>Error</code> objects.
 * 
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 * 
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "UserErrorCode.USER_NOT_FOUND",
 *     description: "Account a9509da9-edf5-4838-acf4-8f1b73485d7a was not found."
 *   }],
 *   success: false
 * }
 * 
 */
function meterMeasurements() { return; }

/**
 * @api {post} /v1/device/measurement/query Search Amphiro measurements
 * @apiVersion 0.0.1
 * @apiName AmphiroMeasurementByTime
 * @apiGroup Search
 * @apiPermission ROLE_USER
 * 
 * @apiDescription Loads measurements for one or more Amphiro devices. <b><span class="note">Amphiro session ordering is time based.</span></b>
 *
 * @apiParam (AmphiroMeasurementTimeIntervalQuery) {Object}     credentials                 User credentials
 * @apiParam (AmphiroMeasurementTimeIntervalQuery) {String}     credentials.username        User name.
 * @apiParam (AmphiroMeasurementTimeIntervalQuery) {String}     credentials.password        Password.
 * @apiParam (AmphiroMeasurementTimeIntervalQuery) {String[]}   deviceKey                  Array of unique identifiers (UUID) for authenticated user Amphiro devices.
 * @apiParam (AmphiroMeasurementTimeIntervalQuery) {Number}     granularity                 Sets the data aggregation level. Valid values are:<br/><br/>
 * 0: <code>NONE</code><br/>
 * 1: <code>HOUR</code><br/>
 * 2: <code>DAY</code><br/>
 * 3: <code>WEEK</code><br/>
 * 4: <code>MONTH</code><br/>
 * 5: <code>YEAR</code><br/>
 * @apiParam (AmphiroMeasurementTimeIntervalQuery) {Number}     startDate                   Time interval start time stamp.
 * @apiParam (AmphiroMeasurementTimeIntervalQuery) {Number}     endDate                     Time interval end time stamp.
 * 
 * @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     "username":"user@daiad.eu",
 *     "password":"****",
 *   },
 *   "deviceKey": [
 *     "114e7af3-39af-4a44-ac57-41779689ec39"
 *   ],
 *   "granularity": 3,
 *   "startDate":1460926800000,
 *   "endDate":1461531599000
 * }
 * 
 * @apiSuccess (AmphiroMeasurementTimeIntervalQueryResult) {Boolean}  success            Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess (AmphiroMeasurementTimeIntervalQueryResult) {Object[]} errors             Array of <code>Error</code>.
 * @apiSuccess (AmphiroMeasurementTimeIntervalQueryResult) {Object[]} series             Array of <code>AmphiroDataSeries</code>.
 * 
 * @apiSuccess (AmphiroDataSeries) {String}               deviceKey          Device unique key (UUID).
 * @apiSuccess (AmphiroDataSeries) {Object[]}             points           Array of <code>AmphiroAbstractDataPoint</code>. if <code>granularity</code> is <code>NONE</code>, instances of <code>AmphiroDataPoint</code> are returned; Otherwise instances of <code>AmphiroAggregatedDataPoint</code> are returned.
 * 
 * @apiSuccess (AmphiroAbstractDataPoint)  {Number}               timestamp          Data point timestamp.
 * @apiSuccess (AmphiroAbstractDataPoint)  {Number}               volume             Volume difference.
 * @apiSuccess (AmphiroAbstractDataPoint)  {Number}               energy             Energy difference.
 * @apiSuccess (AmphiroAbstractDataPoint)  {Number}               temperature        Temperature.
 * 
 * @apiSuccess (AmphiroDataPoint extends AmphiroAbstractDataPoint)           {Number}               sessionId          Unique per Amphiro device session Id to which this measurement belongs to.
 * @apiSuccess (AmphiroDataPoint extends AmphiroAbstractDataPoint)           {Number}               index              Index in measurement series for the current session.
 * @apiSuccess (AmphiroDataPoint extends AmphiroAbstractDataPoint)           {Boolean}              history            Always <code>false</code> since historical sessions have no measurements.
 * 
 * @apiSuccess (AmphiroAggregatedDataPoint extends AmphiroAbstractDataPoint) {Number}               count              Total number of aggregated data points.
 * 
 * @apiSuccessExample {json} Response Example (no aggregation)
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "series": [{
 *     "deviceKey": "4b6bb490-1c03-4c9d-b5d0-1dbb758bf71a",
 *     "points": [{
 *       "timestamp": 1461060000000,
 *       "temperature": 20,
 *       "volume": 1.9,
 *       "energy": 0.4,
 *       "sessionId": 1,
 *       "index": 1,
 *       "history": false
 *     }]
 *   }],
 *   "success": true
 * }
 * 
 * @apiSuccessExample {json} Response Example (with aggregation)
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "series": [{
 *     "deviceKey": "4b6bb490-1c03-4c9d-b5d0-1dbb758bf71a",
 *     "points": [{
 *       "timestamp": 1461448800000,
 *       "temperature": 20,
 *       "volume": 10,
 *       "energy": 11.2,
 *       "count": 5
 *     }]
 *   }],
 *   "success": true
 * }
 * 
 * @apiError {Boolean}  success Always <code>false</code>.
 * @apiError {Object[]} errors Array of <code>Error</code> objects.
 * 
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 * 
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "UserErrorCode.USER_NOT_FOUND",
 *     description: "Account a9509da9-edf5-4838-acf4-8f1b73485d7a was not found."
 *   }],
 *   success: false
 * }
 * 
 */
function amphiroMesurementsByTime() { return; }

/**
 * @api {post} /v2/device/measurement/query Search Amphiro measurements
 * @apiVersion 0.0.2
 * @apiName AmphiroMeasurementByIndex
 * @apiGroup Search
 * @apiPermission ROLE_USER
 * 
 * @apiDescription Loads measurements for one or more Amphiro devices. <b><span class="note">Amphiro session ordering is index based.</span></b>
 *
 * @apiParam (AmphiroMeasurementIndexIntervalQuery) {Object}     credentials                 User credentials
 * @apiParam (AmphiroMeasurementIndexIntervalQuery) {String}     credentials.username        User name.
 * @apiParam (AmphiroMeasurementIndexIntervalQuery) {String}     credentials.password        Password.
 * @apiParam (AmphiroMeasurementIndexIntervalQuery) {String}     credentials.password        Password.
 * @apiParam (AmphiroMeasurementIndexIntervalQuery) {String}     type                        Query type. Valid values are <code>ABSOLUTE</code> and <code>SLIDING</code>.<br/><code>ABSOLUTE</code> queries return specific intervals of sessions.<br/><code>SLIDING</code> queries return the <code>length</code> most recent sessions.
 * @apiParam (AmphiroMeasurementIndexIntervalQuery) {String[]}   deviceKey                   Array of unique identifiers (UUID) for authenticated user Amphiro devices.
 * @apiParam (AmphiroMeasurementIndexIntervalQuery) {Number}     startIndex                  Session start index for <code>ABSOLUTE</code> queries.
 * @apiParam (AmphiroMeasurementIndexIntervalQuery) {Number}     endIndex                    Session end index for <code>ABSOLUTE</code> queries.
 * @apiParam (AmphiroMeasurementIndexIntervalQuery) {Number}     length                      Result size for <code>SLIDING</code>.
 * 
* @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     "username":"user@daiad.eu",
 *     "password":"****",
 *   },
 *   "deviceKey": [
 *     "114e7af3-39af-4a44-ac57-41779689ec39"
 *   ],
 *   "type": "SLIDING",
 *   "length":1
 * }
 * 
 * @apiSuccess (WaterMeterMeasurementQueryResult) {Boolean}  success            Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess (WaterMeterMeasurementQueryResult) {Object[]} errors             Array of <code>Error</code>.
 * @apiSuccess (WaterMeterMeasurementQueryResult) {Object[]} series             Array of <code>AmphiroDataSeries</code>.
 * 
 * @apiSuccess (AmphiroDataSeries)                {String}   deviceKey          Device unique key (UUID).
 * @apiSuccess (AmphiroDataSeries)                {Object[]} points             Array of <code>AmphiroDataPoint</code>.
 * 
 * @apiSuccess (AmphiroDataPoint)                 {Number}               timestamp          Data point timestamp.
 * @apiSuccess (AmphiroDataPoint)                 {Number}               volume             Volume difference.
 * @apiSuccess (AmphiroDataPoint)                 {Number}               energy             Energy difference.
 * @apiSuccess (AmphiroDataPoint)                 {Number}               temperature        Temperature.
 * @apiSuccess (AmphiroDataPoint)                 {Number}               sessionId          Unique per Amphiro device session Id to which this measurement belongs to.
 * @apiSuccess (AmphiroDataPoint)                 {Number}               index              Index in measurement series for the current session.
 * @apiSuccess (AmphiroDataPoint)                 {Boolean}              history            Always <code>false</code> since historical sessions have no measurements.
 * 
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "series": [{
 *     "deviceKey": "4b6bb490-1c03-4c9d-b5d0-1dbb758bf71a",
 *     "points": [{
 *       "timestamp": 1461063910000,
 *       "temperature": 20,
 *       "volume": 3.8,
 *       "energy": 5.7,
 *       "sessionId": 3,
 *       "index": 1,
 *       "history": false
 *     }]
 *   }],
 *   "success": true
 * }
 * 
 * @apiError {Boolean}  success Always <code>false</code>.
 * @apiError {Object[]} errors Array of <code>Error</code> objects.
 * 
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 * 
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "UserErrorCode.USER_NOT_FOUND",
 *     description: "Device 4b6bb490-1c03-4c9d-b5d0-1dbb758bf71a was not found."
 *   }],
 *   success: false
 * }
 * 
 */
function amphiroMesurementsByIndex() { return; }

/**
 * @api {post} /v1/device/session/query Search Amphiro sessions
 * @apiVersion 0.0.1
 * @apiName AmphiroSessionByTime
 * @apiGroup Search
 * @apiPermission ROLE_USER
 * 
 * @apiDescription Loads sessions for one or more Amphiro devices. <b><span class="note">Amphiro session ordering is time based.</span></b>
 *
 * @apiParam (AmphiroSessionCollectionTimeIntervalQuery) {Object}     credentials                 User credentials
 * @apiParam (AmphiroSessionCollectionTimeIntervalQuery) {String}     credentials.username        User name.
 * @apiParam (AmphiroSessionCollectionTimeIntervalQuery) {String}     credentials.password        Password.
 * @apiParam (AmphiroSessionCollectionTimeIntervalQuery) {String[]}   deviceKey                   Array of unique identifiers (UUID) for authenticated user Amphiro devices.
 * @apiParam (AmphiroSessionCollectionTimeIntervalQuery) {Number}     granularity                 Sets the data aggregation level. Valid values are:<br/><br/>
 * 0: <code>NONE</code><br/>
 * 1: <code>HOUR</code><br/>
 * 2: <code>DAY</code><br/>
 * 3: <code>WEEK</code><br/>
 * 4: <code>MONTH</code><br/>
 * 5: <code>YEAR</code><br/>
 * @apiParam (AmphiroSessionCollectionTimeIntervalQuery) {Number}     startDate                   Time interval start time stamp.
 * @apiParam (AmphiroSessionCollectionTimeIntervalQuery) {Number}     endDate                     Time interval end time stamp.
 * 
 * @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     "username":"user@daiad.eu",
 *     "password":"****",
 *   },
 *   "deviceKey": [
 *     "114e7af3-39af-4a44-ac57-41779689ec39"
 *   ],
 *   "granularity": 3,
 *   "startDate":1460926800000,
 *   "endDate":1461531599000
 * }
 * 
 * @apiSuccess (AmphiroSessionCollectionTimeIntervalQueryResult) {Boolean}  success            Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess (AmphiroSessionCollectionTimeIntervalQueryResult) {Object[]} errors             Array of <code>Error</code>.
 * @apiSuccess (AmphiroSessionCollectionTimeIntervalQueryResult) {Object[]} devices            Array of <code>AmphiroSessionCollection</code>.
 * 
 * @apiSuccess (AmphiroSessionCollection) {String}               deviceKey          Device unique key (UUID).
 * @apiSuccess (AmphiroSessionCollection) {String}               name               Device user friendly name.
 * @apiSuccess (AmphiroSessionCollection) {Object[]}             sessions           Array of <code>AmphiroAbstractSession</code>. if <code>granularity</code> is <code>NONE</code>, instances of <code>AmphiroSession</code> are returned; Otherwise instances of <code>AmphiroAggregatedSession</code> are returned.
 * 
 * @apiSuccess (AmphiroAbstractSession)  {Number}               timestamp          Session time stamp.
 * @apiSuccess (AmphiroAbstractSession)  {Number}               duration           Duration.
 * @apiSuccess (AmphiroAbstractSession)  {Number}               volume             Total volume.
 * @apiSuccess (AmphiroAbstractSession)  {Number}               energy             Total energy.
 * @apiSuccess (AmphiroAbstractSession)  {Number}               temperature        Average temperature.
 * @apiSuccess (AmphiroAbstractSession)  {Number}               flow               Average flow.
 * 
 * @apiSuccess (AmphiroSession extends AmphiroAbstractSession)           {Number}    id           Session unique per device id.
 * @apiSuccess (AmphiroSession extends AmphiroAbstractSession)           {Boolean}   history      <code>true</code> if this is a historical session; Otherwise <code>false</code>.
 * @apiSuccess (AmphiroSession extends AmphiroAbstractSession)           {Ojbect[]}  properties   Array of <code>KeyValuePair</code>.
 * 
 * @apiParam (KeyValuePair) {String}           key                         Key.
 * @apiParam (KeyValuePair) {String}           value                       Value.
 * 
 * 
 * @apiSuccess (AmphiroAggregatedSession extends AmphiroAbstractSession) {Number}    count        Total number of aggregated sessions.
 * 
 * @apiSuccessExample {json} Response Example (no aggregation)
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "devices": [{
 *     "deviceKey": "4b6bb490-1c03-4c9d-b5d0-1dbb758bf71a",
 *     "name": "Amphiro #1",
 *     "sessions": [{
 *       "timestamp": 1461060000000,
 *       "duration": 3,
 *       "temperature": 35.1,
 *       "volume": 34.2,
 *       "energy": 10,
 *       "flow": 5,
 *       "id": 1,
 *       "history": false,
 *       "properties": []
 *     }]
 *   }],
 *   "success": true
 * }
 * 
 * @apiSuccessExample {json} Response Example (with aggregation)
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "devices": [{
 *     "deviceKey": "4b6bb490-1c03-4c9d-b5d0-1dbb758bf71a",
 *     "name": "Amphiro #1",
 *     "sessions": [{
 *       "timestamp": 1461016800000,
 *       "duration": 93,
 *       "temperature": 30.433334,
 *       "volume": 432.8,
 *       "energy": 12.314561,
 *       "flow": 3.026327,
 *       "count": 6
 *     }]
 *   }],
 *   "success": true
 * }
 * 
 * @apiError {Boolean}  success Always <code>false</code>.
 * @apiError {Object[]} errors Array of <code>Error</code> objects.
 * 
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 * 
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "UserErrorCode.USER_NOT_FOUND",
 *     description: "Account a9509da9-edf5-4838-acf4-8f1b73485d7a was not found."
 *   }],
 *   success: false
 * }
 * 
 */
function amphiroSessionsByTime() { return; }

/**
 * @api {post} /v2/device/session/query Search Amphiro sessions
 * @apiVersion 0.0.2
 * @apiName AmphiroSessionByIndex
 * @apiGroup Search
 * @apiPermission ROLE_USER
 * 
 * @apiDescription Loads sessions for one or more Amphiro devices. <b><span class="note">Amphiro session ordering is index based.</span></b>
 *
 * @apiParam (AmphiroSessionCollectionIndexIntervalQuery) {Object}     credentials                 User credentials
 * @apiParam (AmphiroSessionCollectionIndexIntervalQuery) {String}     credentials.username        User name.
 * @apiParam (AmphiroSessionCollectionIndexIntervalQuery) {String}     credentials.password        Password.
 * @apiParam (AmphiroSessionCollectionIndexIntervalQuery) {String}     type                        Query type. Valid values are <code>ABSOLUTE</code> and <code>SLIDING</code>.<br/><code>ABSOLUTE</code> queries return specific intervals of sessions.<br/><code>SLIDING</code> queries return the <code>length</code> most recent sessions.
 * @apiParam (AmphiroSessionCollectionIndexIntervalQuery) {String[]}   deviceKey                   Array of unique identifiers (UUID) for authenticated user Amphiro devices.
 * @apiParam (AmphiroSessionCollectionIndexIntervalQuery) {Number}     startIndex                  Session start index for <code>ABSOLUTE</code> queries.
 * @apiParam (AmphiroSessionCollectionIndexIntervalQuery) {Number}     endIndex                    Session end index for <code>ABSOLUTE</code> queries.
 * @apiParam (AmphiroSessionCollectionIndexIntervalQuery) {Number}     length                      Result size for <code>SLIDING</code>.
 * 
 * @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     "username":"user@daiad.eu",
 *     "password":"****",
 *   },
 *   "deviceKey": [
 *     "114e7af3-39af-4a44-ac57-41779689ec39"
 *   ],
 *   "type": "ABSOLUTE",
 *   "startIndex":1,
 *   "endIndex":1
 * }
 * 
 * @apiSuccess (AmphiroSessionCollectionIndexIntervalQueryResult) {Boolean}  success            Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess (AmphiroSessionCollectionIndexIntervalQueryResult) {Object[]} errors             Array of <code>Error</code>.
 * @apiSuccess (AmphiroSessionCollectionIndexIntervalQueryResult) {Object[]} devices            Array of <code>AmphiroSessionCollection</code>.
 * 
 * @apiSuccess (AmphiroSessionCollection) {String}               deviceKey          Device unique key (UUID).
 * @apiSuccess (AmphiroSessionCollection) {String}               name               Device user friendly name.
 * @apiSuccess (AmphiroSessionCollection) {Object[]}             sessions           Array of <code>AmphiroSession</code>.
 * 
 * @apiSuccess (AmphiroSession)  {Number}               id                 Session unique per device id.
 * @apiSuccess (AmphiroSession)  {Boolean}              history            <code>true</code> if this is a historical session; Otherwise <code>false</code>.
 * @apiSuccess (AmphiroSession)  {Number}               timestamp          Session time stamp.
 * @apiSuccess (AmphiroSession)  {Number}               duration           Duration.
 * @apiSuccess (AmphiroSession)  {Number}               volume             Total volume.
 * @apiSuccess (AmphiroSession)  {Number}               energy             Total energy.
 * @apiSuccess (AmphiroSession)  {Number}               temperature        Average temperature.
 * @apiSuccess (AmphiroSession)  {Number}               flow               Average flow.
 * @apiSuccess (AmphiroSession)  {Ojbect[]}             properties         Array of <code>KeyValuePair</code>.
 * @apiSuccess (AmphiroSession)  {Object}               [member]           Household member.
 * 
 * @apiSuccess (KeyValuePair)    {String}               key                Key.
 * @apiSuccess (KeyValuePair)    {String}               value              Value.
 *
 * @apiSuccess (Member)          {Number}               index              Unique household member index.
 * @apiSuccess (Member)          {String}               mode               Indicates the source of the value. Valid values are:
 * </br><code>AUTO</code> : Set to the default user automatically.
 * </br><code>SYSTEM</code> : Computed using an analysis algorithm.
 * </br><code>MANUAL</code> : Set by the user explicitly.
 * @apiSuccess (Member)          {Number}               timestamp          Most recent member assignment time stamp.
 * 
 * 
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "devices": [{
 *     "deviceKey": "4b6bb490-1c03-4c9d-b5d0-1dbb758bf71a",
 *     "name": "Amphiro #1",
 *     "sessions": [{
 *       "timestamp": 1461060000000,
 *       "duration": 3,
 *       "temperature": 35.1,
 *       "volume": 34.2,
 *       "energy": 10,
 *       "flow": 5,
 *       "id": 1,
 *       "history": false,
 *       "properties": [],
 *       "member" : {
 *         "index" : 4,
 *         "mode" : "MANUAL",
 *         "timestamp" : 1461064312345
 *       }
 *     }]
 *   }],
 *   "success": true
 * }
 * 
 * @apiError {Boolean}  success Always <code>false</code>.
 * @apiError {Object[]} errors Array of <code>Error</code> objects.
 * 
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 * 
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "UserErrorCode.USER_NOT_FOUND",
 *     description: "Account a9509da9-edf5-4838-acf4-8f1b73485d7a was not found."
 *   }],
 *   success: false
 * }
 * 
 */
function amphiroSessionsByIndex() { return; }

/**
 * @api {post} /v1/device/session Get Amphiro session
 * @apiVersion 0.0.1
 * @apiName AmphiroSingleSessionByTime
 * @apiGroup Search
 * @apiPermission ROLE_USER
 * 
 * @apiDescription Get a single Amphiro session. A time interval is required for searching for the session. During the query execution, all sessions in the given interval will be scanned until a session with id equal to <code>sessionId</code> is found. The execution performance of this method get worse the longer the time interval becomes.
 *
 * @apiParam (AmphiroSessionTimeIntervalQuery) {Object}     credentials                 User credentials
 * @apiParam (AmphiroSessionTimeIntervalQuery) {String}     credentials.username        User name.
 * @apiParam (AmphiroSessionTimeIntervalQuery) {String}     credentials.password        Password.
 * @apiParam (AmphiroSessionTimeIntervalQuery) {String}     deviceKey                   Device unique id (UUID).
 * @apiParam (AmphiroSessionTimeIntervalQuery) {Number}     sessionId                   Session unique per device id.
 * @apiParam (AmphiroSessionTimeIntervalQuery) {Number}     startDate                   Time interval start time stamp.
 * @apiParam (AmphiroSessionTimeIntervalQuery) {Number}     endDate                     Time interval end time stamp.
 * 
 * @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     "username":"user@daiad.eu",
 *     "password":"****",
 *   },
 *   "deviceKey": "114e7af3-39af-4a44-ac57-41779689ec39",
 *   "sessionId": 2,
 *   "startDate":1460926800000,
 *   "endDate":1461531599000
 * }
 * 
 * @apiSuccess (AmphiroSessionTimeIntervalQueryResult) {Boolean}  success            Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess (AmphiroSessionTimeIntervalQueryResult) {Object[]} errors             Array of <code>Error</code>.
 * @apiSuccess (AmphiroSessionTimeIntervalQueryResult) {Object[]} session            Session object of type <code>AmphiroSessionDetails</code>.
 * 
 * @apiSuccess (AmphiroSessionDetails)    {Number}      id           Session unique per device id.
 * @apiSuccess (AmphiroSessionDetails)    {Boolean}     history      <code>true</code> if this is a historical session; Otherwise <code>false</code>.
 * @apiSuccess (AmphiroSessionDetails)    {Number}      timestamp    Session time stamp.
 * @apiSuccess (AmphiroSessionDetails)    {Number}      duration     Duration.
 * @apiSuccess (AmphiroSessionDetails)    {Number}      volume       Total volume.
 * @apiSuccess (AmphiroSessionDetails)    {Number}      energy       Total energy.
 * @apiSuccess (AmphiroSessionDetails)    {Number}      temperature  Average temperature.
 * @apiSuccess (AmphiroSessionDetails)    {Number}      flow         Average flow.
 * @apiSuccess (AmphiroSessionDetails)    {Ojbect[]}    properties   Array of <code>KeyValuePair</code>.
 * @apiSuccess (AmphiroSessionDetails)    {Object[]}    measurements Array of <code>AmphiroMeasurement</code>.
 * 
 * @apiSuccess (KeyValuePair)             {String}      key                Key.
 * @apiSuccess (KeyValuePair)             {String}      value              Value.
 * 
 * @apiSuccess (AmphiroMeasurement)       {Number}      sessionId          Session unique per device id.
 * @apiSuccess (AmphiroMeasurement)       {Number}      index              Measurement index in session.
 * @apiSuccess (AmphiroMeasurement)       {Boolean}     history            Always <code>false</code> since historical sessions have no measurements.
 * @apiSuccess (AmphiroMeasurement)       {Number}      timestamp          Measurement time stamp.
 * @apiSuccess (AmphiroMeasurement)       {Number}      volume             Volume difference.
 * @apiSuccess (AmphiroMeasurement)       {Number}      energy             Energy difference.
 * @apiSuccess (AmphiroMeasurement)       {Number}      temperature        Current temperature.
 * 
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "session": {
 *     "id": 2,
 *     "history": false,
 *     "timestamp": 1461060300000,
 *     "duration": 2,
 *     "temperature": 21.5,
 *     "volume": 91.8,
 *     "energy": 0.22,
 *     "flow": 0.6,
 *     "properties": [],
 *     "measurements": [{
 *       "sessionId": 2,
 *       "index": 1,
 *       "history": false,
 *       "timestamp": 1461060300000,
 *       "temperature": 20,
 *       "volume": 1.1,
 *       "energy": 3
 *     }]
 *   },
 *   "success": true
 * }
 * 
 * @apiError {Boolean}  success Always <code>false</code>.
 * @apiError {Object[]} errors Array of <code>Error</code> objects.
 * 
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 * 
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "UserErrorCode.USER_NOT_FOUND",
 *     description: "Account a9509da9-edf5-4838-acf4-8f1b73485d7a was not found."
 *   }],
 *   success: false
 * }
 * 
 */
function amphiroSingleSessionByTime() { return; }

/**
 * @api {post} /v2/device/session Get Amphiro session
 * @apiVersion 0.0.2
 * @apiName AmphiroSingleSessionByIndex
 * @apiGroup Search
 * @apiPermission ROLE_USER
 * 
 * @apiDescription Get a single Amphiro session.
 *
 * @apiParam (AmphiroSessionIndexIntervalQuery) {Object}     credentials                 User credentials
 * @apiParam (AmphiroSessionIndexIntervalQuery) {String}     credentials.username        User name.
 * @apiParam (AmphiroSessionIndexIntervalQuery) {String}     credentials.password        Password.
 * @apiParam (AmphiroSessionIndexIntervalQuery) {String}     deviceKey                      Device unique id (UUID).
 * @apiParam (AmphiroSessionIndexIntervalQuery) {Number}     sessionId                      Session unique per device id.
 * 
 * @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     "username":"user@daiad.eu",
 *     "password":"****",
 *   },
 *   "deviceKey": "114e7af3-39af-4a44-ac57-41779689ec39",
 *   "sessionId": 2
 * }
 * 
 * @apiSuccess (AmphiroSessionIndexIntervalQueryResult) {Boolean}  success            Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess (AmphiroSessionIndexIntervalQueryResult) {Object[]} errors             Array of <code>Error</code>.
 * @apiSuccess (AmphiroSessionIndexIntervalQueryResult) {Object[]} session            Session object of type <code>AmphiroSessionDetails</code>.
 * 
 * @apiSuccess (AmphiroSessionDetails)    {Number}      id           Session unique per device id.
 * @apiSuccess (AmphiroSessionDetails)    {Boolean}     history      <code>true</code> if this is a historical session; Otherwise <code>false</code>.
 * @apiSuccess (AmphiroSessionDetails)    {Number}      timestamp    Session time stamp.
 * @apiSuccess (AmphiroSessionDetails)    {Number}      duration     Duration.
 * @apiSuccess (AmphiroSessionDetails)    {Number}      volume       Total volume.
 * @apiSuccess (AmphiroSessionDetails)    {Number}      energy       Total energy.
 * @apiSuccess (AmphiroSessionDetails)    {Number}      temperature  Average temperature.
 * @apiSuccess (AmphiroSessionDetails)    {Number}      flow         Average flow.
 * @apiSuccess (AmphiroSessionDetails)    {Ojbect[]}    properties   Array of <code>KeyValuePair</code>.
 * @apiSuccess (AmphiroSessionDetails)    {Object[]}    measurements Array of <code>AmphiroMeasurement</code>.
 * @apiSuccess (AmphiroSessionDetails)    {Object}      [member]     Household member.
 * 
 * @apiSuccess (KeyValuePair)             {String}      key                Key.
 * @apiSuccess (KeyValuePair)             {String}      value              Value.
 *
 * @apiSuccess (Member)          {Number}               index              Unique household member index.
 * @apiSuccess (Member)          {String}               mode               Indicates the source of the value. Valid values are:
 * </br><code>AUTO</code> : Set to the default user automatically.
 * </br><code>SYSTEM</code> : Computed using an analysis algorithm.
 * </br><code>MANUAL</code> : Set by the user explicitly.
 * @apiSuccess (Member)          {Number}               timestamp          Most recent member assignment time stamp.
 * 
 * @apiSuccess (AmphiroMeasurement)       {Number}      sessionId          Session unique per device id.
 * @apiSuccess (AmphiroMeasurement)       {Number}      index              Measurement index in session.
 * @apiSuccess (AmphiroMeasurement)       {Boolean}     history            Always <code>false</code> since historical sessions have no measurements.
 * @apiSuccess (AmphiroMeasurement)       {Number}      timestamp          Measurement time stamp.
 * @apiSuccess (AmphiroMeasurement)       {Number}      volume             Volume difference.
 * @apiSuccess (AmphiroMeasurement)       {Number}      energy             Energy difference.
 * @apiSuccess (AmphiroMeasurement)       {Number}      temperature        Current temperature.
 * 
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "session": {
 *     "id": 2,
 *     "history": false,
 *     "timestamp": 1461060300000,
 *     "duration": 2,
 *     "temperature": 21.5,
 *     "volume": 91.8,
 *     "energy": 0.22,
 *     "flow": 0.6,
 *     "properties": [],
 *     "measurements": [{
 *       "sessionId": 2,
 *       "index": 1,
 *       "history": false,
 *       "timestamp": 1461060300000,
 *       "temperature": 20,
 *       "volume": 1.1,
 *       "energy": 3
 *     }],
 *     "member" : {
 *       "index" : 4,
 *       "mode" : "MANUAL",
 *       "timestamp" : 1461064312345
 *     }
 *   },
 *   "success": true
 * }
 * 
 * @apiError {Boolean}  success Always <code>false</code>.
 * @apiError {Object[]} errors Array of <code>Error</code> objects.
 * 
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 * 
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "UserErrorCode.USER_NOT_FOUND",
 *     description: "Account a9509da9-edf5-4838-acf4-8f1b73485d7a was not found."
 *   }],
 *   success: false
 * }
 * 
 */
function amphiroSingleSessionByIndex() { return; }
