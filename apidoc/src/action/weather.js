/**
 * @api {get} action/weather/service Get services
 * @apiVersion 0.0.1
 * @apiName WeatherService
 * @apiGroup Weather
 * @apiPermission ROLE_USER, ROLE_ADMIN
 *
 * @apiDescription Returns all registered weather services.
 *
 * @apiParamExample {json} Request Example
 * GET /action/weather/service
 * 
 * @apiSuccess {Boolean}      success             <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]}     errors              Empty array of error messages.
 * @apiSuccess {Object[]}     services            Array of <code>Service</code> objects.
 *
 * @apiSuccess (Service) {Number}   id            Weather service identifier.
 * @apiSuccess (Service) {String}   name          Unique service name.
 * @apiSuccess (Service) {Object[]} utilities     Array of <code>Utility</code> objects. Represents the supported utilities.
 *
 * @apiSuccess (Utility) {Number}   id            Utility identifier.
 * @apiSuccess (Utility) {Number}   key           Utility unique key (UUID).
 * @apiSuccess (Utility) {String}   name          Name.
 * @apiSuccess (Utility) {String}   country       Country.
 * @apiSuccess (Utility) {String}   timezone      Time zone.
 * @apiSuccess (Utility) {String}   locale        Locale.
 * @apiSuccess (Utility) {String}   city          City.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "success": true
 *   "services": [{
 *     "id": 1,
 *     "name": "AEMET",
 *     "utilities": [{
 *       "id": 2,
 *       "key": "2b48083d-6f05-488f-9f9b-99607a93c6c3",
 *       "name": "Alicante",
 *       "country": "Spain",
 *       "timezone": "Europe/Madrid",
 *       "locale": "es",
 *       "city": "Alicante"
 *     }]
 *   }],
 * }
 *
 * @apiError {Boolean} success Always <code>false</code>.
 * @apiError {Object[]} errors Array of <code>Error</code> objects.
 *
 * @apiError (Error) {String}   code          Unique error code.
 * @apiError (Error) {String}   description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 *
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "SharedErrorCode.AUTHENTICATION_USERNAME",
 *     description: "Authentication has failed for user user@daiad.eu."
 *   }],
 *   success: false
 * }
 */
function getWeatherService() { return; }

/**
 * @api {get} action/weather/{service}/{utility}/{interval}/{from}/{to} Query data
 * @apiVersion 0.0.1
 * @apiName WeatherData
 * @apiGroup Weather
 * @apiPermission ROLE_USER, ROLE_ADMIN
 *
 * @apiDescription Returns weather data from a specific weather service and for the specified utility and time interval.
 * 
 * @apiParam (QueryString)  {String}      service   Service identifier as a number or unique service name.
 * @apiParam (QueryString)  {String}      utility   Utility identifier as a number or unique utility name.
 * @apiParam (QueryString)  {String}      interval  Interval type. Valid values are <code>hour</code> and <code>day</code>.
 * @apiParam (QueryString)  {String}      from      Start date formatted as <code>yyyyMMdd</code>.
 * @apiParam (QueryString)  {String}      to        End date formatted as <code>yyyyMMdd</code>.
 * 
 * @apiParamExample {json} Request Example
 * GET action/weather/aemet/alicante/hour/20160829/20160905
 *
 * @apiSuccess {Boolean}      success               <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]}     errors                Empty array of error messages.
 * @apiSuccess {Object[]}     days                  Array of <code>DailyWeatherData</code> objects. Always <code>null</code> if interval is set to <code>hour</code>.
 * @apiSuccess {Object[]}     hours                 Array of <code>HourlyWeatherData</code> objects. Always <code>null</code> if interval is set to <code>day</code>.
 *
 * @apiSuccess (DailyWeatherData) {String}  date                Date formatted as <code>yyyyMMdd</code>.
 * @apiSuccess (DailyWeatherData) {Number}  minTemperature      Minimum temperature.
 * @apiSuccess (DailyWeatherData) {Number}  maxTemperature      Maximum temperature.
 * @apiSuccess (DailyWeatherData) {Number}  minTemperatureFeel  Minimum feel like temperature.
 * @apiSuccess (DailyWeatherData) {Number}  maxTemperatureFeel  Maximum feel like temperature.
 * @apiSuccess (DailyWeatherData) {Number}  precipitation       Precipitation.
 * @apiSuccess (DailyWeatherData) {Number}  minHumidity         Minimum Humidity.
 * @apiSuccess (DailyWeatherData) {Number}  maxHumidity         Maximum Humidity.
 * @apiSuccess (DailyWeatherData) {Number}  windSpeed           Wind speed.
 * @apiSuccess (DailyWeatherData) {String}  windDirection       Wind direction.
 * @apiSuccess (DailyWeatherData) {String}  conditions          Generic description of the weather conditions. Text localization depends if the weather service supports localization.
 * 
 * @apiSuccess (HourlyWeatherData) {String}   datetime          Date and time formated as <code>yyyyMMddHH</code>. 
 * @apiSuccess (HourlyWeatherData) {Number}   temperature       Temperature
 * @apiSuccess (HourlyWeatherData) {Number}   temperatureFeel   Feel like temperature.
 * @apiSuccess (HourlyWeatherData) {Number}   precipitation     Precipitation.
 * @apiSuccess (HourlyWeatherData) {Number}   humidity          Humidity.
 * @apiSuccess (HourlyWeatherData) {Number}   windSpeed         Wind speed.
 * @apiSuccess (HourlyWeatherData) {String}   windDirection     Wind direction.
 * @apiSuccess (HourlyWeatherData) {String}   conditions        Generic description of the weather conditions. Text localization depends if the weather service supports localization.
 *
 * @apiSuccessExample {json} Response Example (Hourly Weather Data)
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "days": null,
 *   "hours": [{
 *     "temperature": 30,
 *     "temperatureFeel": 33,
 *     "precipitation": 0,
 *     "humidity": 60,
 *     "windSpeed": 15,
 *     "windDirection": "E",
 *     "conditions": null,
 *     "datetime": "2016082912"
 *   }]
 *  }
 *
 * @apiSuccessExample {json} Response Example (Daily Weather Data)
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "hours": null,
 *   "days": [{
 *     "date": "20160829",
 *     "minTemperature": 21,
 *     "maxTemperature": 31,
 *     "minTemperatureFeel": 21,
 *     "maxTemperatureFeel": 34,
 *     "precipitation": null,
 *     "minHumidity": 55,
 *     "maxHumidity": 100,
 *     "windSpeed": null,
 *     "windDirection": null,
 *     "conditions": null
 *   }]
 *  }
 * 
 * @apiError {Boolean} success Always <code>false</code>.
 * @apiError {Object[]} errors Array of <code>Error</code> objects.
 *
 * @apiError (Error) {String}   code          Unique error code.
 * @apiError (Error) {String}   description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 *
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "SharedErrorCode.AUTHENTICATION_USERNAME",
 *     description: "Authentication has failed for user user@daiad.eu."
 *   }],
 *   success: false
 * }
 */
function getWeatherData() { return; }
