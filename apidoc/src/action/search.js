/**
 * @api {post} action/meter/status Meter current status
 * @apiVersion 0.0.1
 * @apiName MeterStatus
 * @apiGroup Search
 * @apiPermission ROLE_USER, ROLE_UTILITY_ADMIN, ROLE_SYSTEM_ADMIN
 *
 * @apiDescription Loads the most recent status of one or more smart water meters.
 *
 * @apiParam (WaterMeterStatusQuery) {String}     [userKey]      User unique key (UUID). If the user key is not set, the key of the authenticated user is used. If the user is not an adiministrator, the user key is automatically overriden by the authenticated user's key.
 * @apiParam (WaterMeterStatusQuery) {String[]}   deviceKey      Array of unique identifiers (UUID) for meter devices. If the device key array is empty or null, the keys of the meters for the current user are used.
 *
 * @apiParamExample {json} Request Example
 * {
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
function getMeterStatus() { return; }

/**
 * @api {post} action/meter/history Search meter measurements
 * @apiVersion 0.0.1
 * @apiName MeterMeasurements
 * @apiGroup Search
 * @apiPermission ROLE_USER, ROLE_UTILITY_ADMIN, ROLE_SYSTEM_ADMIN
 *
 * @apiDescription Loads measurements for one or more meter devices.
 *
 * @apiParam (WaterMeterMeasurementQuery) {String}     [userKey]      User unique key (UUID). If the user key is not set, the key of the authenticated user is used. If the user is not an adiministrator, the user key is automatically overriden by the authenticated user's key.
 * @apiParam (WaterMeterMeasurementQuery) {String[]}   deviceKey      Array of unique identifiers (UUID) for meter devices. If the device key array is empty or null, the keys of the meters for the current user are used.
 * @apiParam (WaterMeterMeasurementQuery) {Number}     granularity    Sets the data aggregation level. Valid values are:<br/><br/>
 * 0: <code>NONE</code><br/>
 * 1: <code>HOUR</code><br/>
 * 2: <code>DAY</code><br/>
 * 3: <code>WEEK</code><br/>
 * 4: <code>MONTH</code><br/>
 * 5: <code>YEAR</code><br/>
 * @apiParam (WaterMeterMeasurementQuery) {Number}     [startDate]    Time interval start time stamp.
 * @apiParam (WaterMeterMeasurementQuery) {Number}     [endDate]      Time interval end time stamp.
 *
 * @apiParamExample {json} Request Example
 * {
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
function getMeterMeasurements() { return; }

/**
 * @api {post} action/device/index/session/query Search Amphiro sessions
 * @apiVersion 0.0.2
 * @apiName Sessions
 * @apiGroup Search
 * @apiPermission ROLE_USER, ROLE_UTILITY_ADMIN, ROLE_SYSTEM_ADMIN
 *
 * @apiDescription Loads sessions for one or more Amphiro devices. <b><span class="note">Amphiro session ordering is index based.</span></b>
 *
 * @apiParam (AmphiroSessionCollectionIndexIntervalQuery) {String}     type         Query type. Valid values are <code>ABSOLUTE</code> and <code>SLIDING</code>.<br/><code>ABSOLUTE</code> queries return specific intervals of sessions.<br/><code>SLIDING</code> queries return the <code>length</code> most recent sessions.
 * @apiParam (AmphiroSessionCollectionIndexIntervalQuery) {String}      [userKey]    User unique key (UUID). If the user key is not set, the key of the authenticated user is used. If the user is not an adiministrator, the user key is automatically overriden by the authenticated user's key.
 * @apiParam (AmphiroSessionCollectionIndexIntervalQuery) {String[]}    deviceKey    Array of unique identifiers (UUID) for Amphiro b1 devices. If the device key array is empty or null, the keys of the devices for the current user are used.
 * @apiParam (AmphiroSessionCollectionIndexIntervalQuery) {Number}      startIndex   Start index for <code>ABSOLUTE</code> queries or offset index for <code>SLIDING</code> ones. Sessions are returned in reverse order with the most recent one returned first.
 * @apiParam (AmphiroSessionCollectionIndexIntervalQuery) {Number}      endIndex     End index for <code>ABSOLUTE</code> queries.
 * @apiParam (AmphiroSessionCollectionIndexIntervalQuery) {Number}      length       Result size for <code>SLIDING</code> queries.
 * @apiParam (AmphiroSessionCollectionIndexIntervalQuery) {Number[]}    [members]    Optional list of household member indexes used for filtering session records.
 *
 * @apiParamExample {json} Request Example
 * {
 *   "deviceKey": [
 *     "114e7af3-39af-4a44-ac57-41779689ec39"
 *   ],
 *   "type": "ABSOLUTE",
 *   "startIndex":1,
 *   "endIndex":1,
 *   "members": [1, 2]
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
function getSessions() { return; }

/**
 * @api {post} action/device/index/session Get Amphiro session
 * @apiVersion 0.0.2
 * @apiName Session
 * @apiGroup Search
 * @apiPermission ROLE_USER
 *
 * @apiDescription Get a single Amphiro session.
 *
 * @apiParam (AmphiroSessionIndexIntervalQuery) {String}     deviceKey            Device unique id (UUID).
 * @apiParam (AmphiroSessionIndexIntervalQuery) {Number}     sessionId            Session unique (per device) id.
 * @apiParam (AmphiroSessionIndexIntervalQuery) {Boolean}    excludeMeasurements  <code>true</code> if measurements should not be returned.
 *
 * @apiParamExample {json} Request Example
 * {
 *   "deviceKey": "114e7af3-39af-4a44-ac57-41779689ec39",
 *   "sessionId": 2,
 *   "excludeMeasurements": false
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
function getSession() { return; }
