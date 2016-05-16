/**
 * @apiDefine ROLE_ADMIN
 *
 * @apiVersion 0.0.1
 */

/**
 * @api {post} v1/data/query Query execution
 * @apiVersion 0.0.1
 * @apiName DataQuery
 * @apiGroup Data
 * @apiPermission ROLE_ADMIN
 *
 * @apiDescription Executes a query for Smart Water Meter or/and Amphiro data. Users can apply consumer, spatial and time constraints. Currenlty spatial filters are evaluated against the selected user' smart water meter location. Population filters refer to single users, groups of users or utilities. Clusters are treated as a special case of a group filter where every cluster is replaced by a new population filter per cluster group. Time filter are either absolute or relative (sliding time window).
 *
 * @apiParam {Object} credentials          User credentials used for authentication.
 * @apiParam {String} credentials.username User name
 * @apiParam {String} credentials.password User password
 * 
 * @apiParam {String} [timezone] Results are converted to the specified time zone.
 * @apiParam {Object} time Time constraint.
 * @apiParam {String} time.type Time constraint type. Valid values are <code>ABSOLUTE</code> or <code>SLIDING</code>. An absolute time constraint requires the <code>start</code> and <code>end</code> parameters. A sliding time constraint requires <code>start</code>, <code>duration</code> and <code>durationTimeUnit</code> parameters.
 * @apiParam {String} time.granularity Defines the time interval over which the computation is applied. Valid values are <code>HOUR</code>, <code>DAY</code>, <code>WEEK</code>, <code>MONTH</code>, <code>YEAR</code>, <code>ALL</code>.<br/><br/>For instance, given an <code>ABSOLUTE</code> time constraint with granularity <code>DAY</code>, the computation will be applied for every day in this interval. The final result will have a single data point for every day in the time interval.
 * @apiParam {Number} time.start Start date as a UTC time stamp.
 * @apiParam {Number} time.end End date as a UTC time stamp. Required only by <code>ABSOLUTE</code> time constraints.
 * @apiParam {Number} time.duration Time interval duration. Required only by <code>SLIDING</code> time constraints. Negative values are allowed.
 * @apiParam {String} time.durationTimeUnit Time interval duration unit. Required only by <code>SLIDING</code> time constraints.
 *
 * @apiParam {Object[]} population Consumer selection filter. An new data series is returned for every population filter. Population filter classes derive from <code>PopulationFilter</code>. Concrete implementations are <code>UserPopulationFilter</code>, <code>GroupPopulationFilter</code>, <code>ClusterPopulationFilter</code> and <code>UtilityPopulationFilter</code>.<br/></br>For additional details see specific types below.
 * 
 * @apiParam {Object} spatial Spatial constraint used for filtering consumers.
 * @apiParam {String} spatial.type Spatial filter operation. Valid values are <code>CONTAINS</code>, <code>INTERSECT</code> and <code>DISTANCE</code>.
 * @apiParam {Object} spatial.geometry Geometry used in spatial filter expressed in GeoJSON.
 * @apiParam {Number} spatial.distance Distance between the consumer's location and the specified geometry. Required when <code>DISTANCE</code> type is selected.
 *
 * @apiParam {String} source Data source. Valid values are <code>BOTH</code>, <code>AMPHIRO</code> and <code>METER</code>. A data series is returned for every data source type.
 *
 * @apiParam {String} metrics Operations applied. Valid values are <code>COUNT</code>, <code>SUM</code>, <code>MIN</code>, <code>MAX</code> or <code>AVERAGE</code>.
 *
 * @apiParam (PopulationFilter) {String} type Filter type. Valid values are <code>USER</code>, <code>GROUP</code>, <code>CLUSTER</code> and <code>UTILITY</code>.
 * <br/><br/><code>USER</code> applies the computation on a single user or a list of users.
 * <br/><br/><code>GROUP</code> applies the computation on all members of a group.
 * <br/><br/><code>CLUSTER</code> applies the computation on all groups of a cluster. It is equivelant of creating a <code>GROUP</code> filter for every cluster group. If more than one of the properties <code>cluster</code>, <code>clusterType</code>, <code>name</code> is set in the implementation class <code>ClusterPopulationFilter</code>, the first in the list overrides the next ones.
 * <br/><br/><code>UTILITY</code> applies the computation on all users of a utility.
 * 
 * @apiParam (PopulationFilter) {String} label User friendly name returned for each data series.
 * 
 * @apiParam (PopulationFilter) {Object} ranking When present instead of aggregating data from all users, user sorted ranking is computed.
 * @apiParam (PopulationFilter) {String} ranking.type Ordering. Can be <code>TOP</code> or <code>BOTTOM</code>,
 * @apiParam (PopulationFilter) {String} ranking.field Field that the computation is applied on. Valid values are <code>VOLUME</code>, <code>ENERGY</code>, <code>DURATION</code>, <code>TEMPERATURE</code> or <code>FLOW</code>.
 * <br/><br/><span class="important"><b>For smart water meters, only VOLUME is supported.</b></span>
 * @apiParam (PopulationFilter) {String} ranking.metric Operation to apply. Valid values are <code>COUNT</code>, <code>SUM</code>, <code>MIN</code>, <code>MAX</code> or <code>AVERAGE</code>.
 * <br/><br/><span class="important"><b>For smart water meters only SUM is supported.</b></span>
 * @apiParam (PopulationFilter) {Number} ranking.limit Number of users to return.
 *
 * @apiParam (UserPopulationFilter extends PopulationFilter) {String[]} users Array of user unique identifiers (UUID).
 *
 * @apiParam (GroupPopulationFilter extends PopulationFilter) {String} group Group unique identifier (UUID).
 *
 * @apiParam (ClusterPopulationFilter extends PopulationFilter) {String} cluster Cluster unique identifier (UUID).
 * @apiParam (ClusterPopulationFilter extends PopulationFilter) {String} clusterType Cluster type. Valid values are <code>AGE</code>, <code>INCOME</code>, <code>HOUSEHOLD_SIZE</code> and <code>APARTMENT_SIZE</code>.
 * @apiParam (ClusterPopulationFilter extends PopulationFilter) {String} name Cluster user friendly name.
 *
 * @apiParam (UtilityPopulationFilter extends PopulationFilter) {String} utility Utility unique identifier (UUID). Required when type is <code>UTILITY</code>.
 * 
 * @apiParamExample {json} Request Example
 *   {
 *      time: {
 *        type : 'SLIDING',
 *        start: moment().valueOf(),
 *        duration: -60,
 *        durationTimeUnit: 'DAY',
 *        granularity: 'DAY'
 *      },
 *      population: [{
 *        type :'USER',
 *        label: 'User 1',
 *        users: ['63078a88-f75a-4c5e-8d75-b4472ba456bb']
 *      }, {
 *        type :'CLUSTER',
 *        label: 'Income',
 *        clusterType: 'INCOME'
 *      }, {
 *        type :'UTILITY',
 *        label: 'Alicante (all)',
 *        utility: '2b48083d-6f05-488f-9f9b-99607a93c6c3'
 *      }, {
 *        type :'UTILITY',
 *        label: 'Alicante (top 2)',
 *        utility: '2b48083d-6f05-488f-9f9b-99607a93c6c3',
 *        ranking: {
 *          type: 'TOP',
 *          metric: 'AVERAGE',
 *          field: 'TEMPERATURE',
 *          limit: 2
 *        }
 *      }],
 *      spatial: {
 *        type: 'CONTAINS',
 *        geometry: {
 *          'type': 'Polygon',
 *          'coordinates': [
 *            [
 *              [
 *                  -0.525970458984375,
 *                  38.329537722849636
 *              ], [
 *                  -0.5233955383300781,
 *                  38.36386812314455
 *              ], [
 *                  -0.4821968078613281,
 *                  38.37651914591569
 *              ], [
 *                  -0.4440879821777344,
 *                  38.33963658855894
 *              ], [
 *                  -0.46966552734375,
 *                  38.31647443592999
 *              ], [
 *                  -0.5089759826660156,
 *                   38.313511301083466
 *              ], [
 *                  -0.525970458984375,
 *                  38.329537722849636
 *              ]
 *            ]
 *          ]
 *        }
 *      },
 *      source: 'METER',
 *      metrics: ['COUNT', 'SUM', 'MAX']
 *    }
 *
 * @apiSuccess {Boolean}  success                  <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors                   Empty array of error messages.
 * @apiSuccess {Object[]} devices                  Collection of <code>DataSeries</code> series with Amphiro data.
 * @apiSuccess {Object[]} meters                   Collection of <code>DataSeries</code> series with smart water meter data.
 * 
 * @apiSuccess (DataSeries) {String} label            Result user friendly name as declared in the query.
 * @apiSuccess (DataSeries) {Number} population       Number of unique users found. This field is the number of users that contributed data to the final result. For instance a group may have 100 members but the <code>population</code> value may be less than 100. This may occur if a spatial field has been applied.
 * @apiSuccess (DataSeries) {Object[]} points           Data points. All data point classes dervice from <code>DataPoint</code>. A response may contain instances of <code>MeterDataPoint</code>, <code>AmphiroDataPoint</code> or <code>RankingDataPoint</code>.
 * 
 * @apiSuccess (DataPoint) {String} type Data point type. Valid values are <code>METER</code>, <code>AMPHIRO</code>, <code>RANKING</code>. Based on the type, the implementing class is <code>MeterDataPoint</code>, <code>AmphiroDataPoint</code> and <code>RankingDataPoint</code> respectively.<br/><br/>Instances of <code>MeterDataPoint</code> are returned for meter results.<br/><br/>Instances of <code>AmphiroDataPoint</code> are returned for Amphiro results.<br/><br/>Instances of <code>RankingDataPoint</code> are returned in any series for which the <code>ranking</code> property is set for the corresponding <code>population</code> filter.
 * @apiSuccess (DataPoint) {Number} timestamp Time stamp.
 * 
 * @apiSuccess (MeterDataPoint extends DataPoint) {Object} volume Volume data.
 * @apiSuccess (MeterDataPoint extends DataPoint) {Object} [volume.MIN] Minimum total water consumption.
 * @apiSuccess (MeterDataPoint extends DataPoint) {Object} [volume.MAX] Maximum total water consumption.
 * @apiSuccess (MeterDataPoint extends DataPoint) {Object} [volume.SUM] Total water consumption.
 * @apiSuccess (MeterDataPoint extends DataPoint) {Object} [volume.COUNT] Number of unique users that contributed to the result.
 * @apiSuccess (MeterDataPoint extends DataPoint) {Object} [volume.AVERAGE] Average water consumption. 
 * 
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Object} volume Volume data.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [volume.MIN] Minimum session consumption.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [volume.MAX] Maximum session consumption.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [volume.SUM] Total consumption for all sessions.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [volume.COUNT] Number of sessions.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [volume.AVERAGE] Average water consumption per session.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Object} energy Energy data.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [energy.MIN] Minimum energy consumption.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [energy.MAX] Maximum energy consumption.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [energy.SUM] Total energy consumption for all sessions.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [energy.COUNT] Number of sessions.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [energy.AVERAGE] Average energy consumption per session.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Object} duration Duration data.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [duration.MIN] Minimum session duration.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [duration.MAX] Maximum session duration.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [duration.SUM] Total duration of all sessions.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [duration.COUNT] Number of sessions.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [duration.AVERAGE] Average session duration. 
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Object} temperature Temperature data.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [temperature.MIN] Minimum water temperature.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [temperature.MAX] Maximum water temperature.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [temperature.COUNT] Number of sessions.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [temperature.AVERAGE] Average water temperature for all sessions. 
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Object} flow Flow data.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [flow.MIN] Minimum water flow.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [flow.MAX] Maximum water flow.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [flow.COUNT] Number of sessions.
 * @apiSuccess (AmphiroDataPoint extends DataPoint) {Number} [flow.AVERAGE] Average water flow for all sessions. 
 * 
 * @apiSuccess (RankingDataPoint) {Object[]} users Unique users. Depending on the source of data (<code>METER</code> or <code>AMPHIRO</code>), the user data points are of type <code>MeterUserDataPoint</code> or <code>AmphiroUserDataPoint</code>. Both classes derive from <code>UserDataPoint</code>.
 * 
 * @apiSuccess (UserDataPoint) {String} key Unique user key (UUID).
 * @apiSuccess (UserDataPoint) {String} label User friendly name. The username account is returned by default.
 * 
 * @apiSuccess (MeterUserDataPoint extends UserDataPoint) {Object} volume Volume data.
 * @apiSuccess (MeterUserDataPoint extends UserDataPoint) {Number} volume.SUM Total water consumption.
 * 
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Object} volume Volume data.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [volume.MIN] Minimum session consumption.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [volume.MAX] Maximum session consumption.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [volume.SUM] Total consumption for all sessions.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [volume.COUNT] Number of sessions.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [volume.AVERAGE] Average water consumption per session.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Object} energy Energy data.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [energy.MIN] Minimum energy consumption.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [energy.MAX] Maximum energy consumption.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [energy.SUM] Total energy consumption for all sessions.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [energy.COUNT] Number of sessions.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [energy.AVERAGE] Average energy consumption per session.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Object} duration Duration data.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [duration.MIN] Minimum session duration.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [duration.MAX] Maximum session duration.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [duration.SUM] Total duration of all sessions.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [duration.COUNT] Number of sessions.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [duration.AVERAGE] Average session duration. 
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Object} temperature Temperature data.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [temperature.MIN] Minimum water temperature.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [temperature.MAX] Maximum water temperature.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [temperature.COUNT] Number of sessions.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [temperature.AVERAGE] Average water temperature for all sessions. 
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Object} flow Flow data.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [flow.MIN] Minimum water flow.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [flow.MAX] Maximum water flow.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [flow.COUNT] Number of sessions.
 * @apiSuccess (AmphiroUserDataPoint extends UserDataPoint) {Number} [flow.AVERAGE] Average water flow for all sessions. 
 * 
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "timezone": "Europe/Madrid",
 *   "devices": [
 *     {
 *       "label": "User 1",
 *       "population": 1,
 *       "points": [
 *         {
 *           "type": "AMPHIRO",
 *           "timestamp": 1460498400000,
 *           "volume": {
 *             "AVERAGE": 13.5,
 *             "MIN": 13,
 *             "SUM": 27,
 *             "MAX": 14,
 *             "COUNT": 2
 *           },
 *           "duration": {
 *             "AVERAGE": 156,
 *             "MIN": 145,
 *             "SUM": 312,
 *             "MAX": 167,
 *             "COUNT": 2
 *           },
 *           "temperature": {
 *             "AVERAGE": 36.5,
 *             "MIN": 36,
 *             "MAX": 37,
 *             "COUNT": 2
 *           },
 *           "energy": {
 *             "AVERAGE": 0.2614302486181259,
 *             "MIN": 0.24384525418281555,
 *             "SUM": 0.5228604972362518,
 *             "MAX": 0.2790152430534363,
 *             "COUNT": 2
 *           },
 *           "flow":                {
 *             "AVERAGE": 5.204625129699707,
 *             "MIN": 5.029940128326416,
 *             "MAX": 5.379310131072998,
 *             "COUNT": 2
 *           }
 *         }
 *       ]
 *     }
 *   ],
 *   "meters":[
 *     {
 *       "label": "User 1",
 *       "population": 1,
 *       "points": [
 *          {
 *             "type": "METER",
 *             "timestamp": 1462831200000,
 *             "volume":                {
 *                "AVERAGE": 84,
 *                "MIN": 84,
 *                "SUM": 84,
 *                "MAX": 84,
 *                "COUNT": 1
 *             }
 *          }, {
 *             "type": "METER",
 *             "timestamp": 1462744800000,
 *             "volume":                {
 *                "AVERAGE": 2,
 *                "MIN": 2,
 *                "SUM": 2,
 *                "MAX": 2,
 *                "COUNT": 1
 *             }
 *          }
 *       ]
 *    }, {
 *       "label": "Alicante",
 *       "population": 15,
 *       "points": [
 *          {
 *             "type": "METER",
 *             "timestamp": 1462831200000,
 *             "volume":                {
 *                "AVERAGE": 44,
 *                "MIN": 1,
 *                "SUM": 440,
 *                "MAX": 133,
 *                "COUNT": 10
 *             }
 *          }, {
 *             "type": "METER",
 *             "timestamp": 1462572000000,
 *             "volume":                {
 *                "AVERAGE": 20,
 *                "MIN": 7,
 *                "SUM": 180,
 *                "MAX": 58,
 *                "COUNT": 9
 *             }
 *          }
 *       ]
 *     }
 *   ],
 *   "success": true
 * }
 * 
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "timezone": "Europe/Madrid",
 *   "devices": [ {
 *     "label": "Alicante",
 *     "population": 15,
 *     "points": [
 *       {
 *         "type": "RANKING",
 *         "timestamp": 1464732000000,
 *         "users": [ {
 *           "key": "d7fb2365-5a7f-41b4-a6fb-211f8f69931f",
 *           "label": "piramidedemarcos@gmail.com",
 *           "volume": {
 *             "COUNT": 1,
 *             "AVERAGE": 61,
 *             "SUM": 61
 *           },
 *           "duration": {
 *             "COUNT": 1,
 *             "AVERAGE": 295,
 *             "SUM": 295
 *           },
 *           "temperature": {
 *             "COUNT": 1,
 *             "AVERAGE": 37
 *           },
 *           "energy": {
 *             "COUNT": 1,
 *             "AVERAGE": 1.2157092094421387,
 *             "SUM": 1.2157092094421387
 *           },
 *             "flow": {
 *             "COUNT": 1,
 *             "AVERAGE": 1.2406799793243408
 *           }
 *         } ]
 *       }
 *     ]
 *   } ],
 *   "meters": [ {
 *     "label": "Alicante",
 *     "population": 15,
 *     "points": [ {
 *       "type": "RANKING",
 *       "timestamp": 1462053600000,
 *       "users": [ {
 *         "key": "92c74655-a606-4f29-aa56-cc478aa730fb",
 *         "label": "vjuan.martinez@aguasdealicante.es",
 *         "volume": {"SUM": 22}
 *        } ]
 *      }, {
 *        "type": "RANKING",
 *        "timestamp": 1459461600000,
 *        "users": [ {
 *          "key": "a203c47d-15ec-4cc1-b2aa-f2438042e619",
 *          "label": "antonio.sanchez@aguasdealicante.es",
 *          "volume": {"SUM": 9}
 *          } ]
 *      } ]
 *   } ],
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
 *     HTTP/1.1 200 OK
 *     {
 *       errors: [{
 *         code: "UserErrorCode.USERNANE_NOT_FOUND",
 *         description: "Account a9509da9-edf5-4838-acf4-8f1b73485d7a was not found."
 *       }],
 *       success: false
 *     }
 */
function query() { return; }

/**
 * @api {post} v1/data/store Store (by time)
 * @apiVersion 0.0.1
 * @apiName DataStoreByTime
 * @apiGroup Data
 * @apiPermission ROLE_USER, ROLE_ADMIN
 * 
 * @apiDescription Store data for Smart Water Meter or Amphiro devices. Accessing Amphiro devices requires <code>ROLE_USER</code> permission. Accessing Smart Water Meter devices requires <code>ROLE_ADMIN</code> permission. Data for meters and Amphiro devices are of type <code>AmphiroMeasurementCollection</code> and <code>WaterMeterMeasurementCollection</code> respectively. Both classes extend <code>DeviceMeasurementCollection</code>. <b><span class="note">Amphiro session ordering is time based.</span></b>
 *
 * @apiParam (DeviceMeasurementCollection) {Object} credentials     User credentials
 * @apiParam (DeviceMeasurementCollection) {String} credentials.username        User name
 * @apiParam (DeviceMeasurementCollection) {String} credentials.password        User password
 * @apiParam (DeviceMeasurementCollection) {String} type            Device type. Valid values are <code>METER</code> and <code>AMPHIRO</code>.
 * @apiParam (DeviceMeasurementCollection) {String} deviceKey       Device unique id (UUID).
 * 
 * @apiParam (WaterMeterMeasurementCollection extends DeviceMeasurementCollection) {Object[]} measurements      Array of <code>WaterMeterMeasurement</code> representing meter measurements.
 * 
 * @apiParam (WaterMeterMeasurement) {Number}   timestamp   Measurement time stamp.
 * @apiParam (WaterMeterMeasurement) {Number}   volume      Volume.
 * @apiParam (WaterMeterMeasurement) {Number}   difference  Difference between the current and previous measurements.
 * 
 * @apiParam (AmphiroMeasurementCollection extends DeviceMeasurementCollection) {Object[]} sessions      Array of <code>AmphiroSession</code> representing Amphiro sessions.
 * @apiParam (AmphiroMeasurementCollection extends DeviceMeasurementCollection) {Object[]} measurements  Array of <code>AmphiroMeasurement</code> representing Amphiro detailed measurements.
 * 
 * @apiParam (AmphiroSession) {Number}       id                    Unique per device session id.
 * @apiParam (AmphiroSession) {Boolean}      history               Set to <code>false</code> for real time sessions; Otherwise <code>true</code>.
 * @apiParam (AmphiroSession) {Object[]}     properties            Session properties represented by an array of <code>KeyValuePair</code> objects.
 * @apiParam (AmphiroSession) {Number}       timestamp             Time stamp at which the mobile application has fetched the session data from the device over Bluetooth.
 * @apiParam (AmphiroSession) {Number}       volume                Total water volume.
 * @apiParam (AmphiroSession) {Number}       energy                Total energy.
 * @apiParam (AmphiroSession) {Number}       duration              Session duration.
 * @apiParam (AmphiroSession) {Number}       temperature           Average temperature.
 * @apiParam (AmphiroSession) {Number}       flow                  Average flow.
 * @apiParam (AmphiroSession) {Object}       delete                If present, existing data for this session must be replaced. This property is used for converting historical sessions to real time ones.
 * @apiParam (AmphiroSession) {Number}       delete.timestamp      Time stamp of the session to be replaced.
 * 
 * @apiParam (KeyValuePair) {String}         key                   Key.
 * @apiParam (KeyValuePair) {String}         value                 Value.
 * 
 * @apiParam (AmphiroMeasurement) {Number}   sessionId             Session id.
 * @apiParam (AmphiroMeasurement) {Number}   index                 Measurement index.
 * @apiParam (AmphiroMeasurement) {Boolean}  history               Set to <code>false</code> for real time sessions; Otherwise <code>true</code>.
 * @apiParam (AmphiroMeasurement) {Number}   timestamp             Time stamp.
 * @apiParam (AmphiroMeasurement) {Number}   temperature           Temperature.
 * @apiParam (AmphiroMeasurement) {Number}   volume                Total water volume.
 * @apiParam (AmphiroMeasurement) {Number}   energy                Total energy.
 * 
 * @apiParamExample {json} Request Example (Amphiro)
 * {
 *   "deviceKey": "4b6bb490-1c03-4c9d-b5d0-1dbb758bf71a",
 *   "type":"AMPHIRO",
 *   "credentials": {
 *     username: "user@daiad.eu",
 *     password: "****"
 *   },
 *   "sessions":[{
 *      "id":1,
 *      "timestamp":1461060000000,
 *      "duration":3,
 *      "history":false,
 *      "temperature":35.1,
 *      "volume":34.2,
 *      "energy":10,
 *      "flow":5,
 *      "properties":[]
 *   }],
 *      "measurements":[{
 *      "sessionId":1,
 *      "index":1,
 *      "history":false,
 *      "timestamp":1461060000000,
 *      "temperature":20,
 *      "volume":1.9,
 *      "energy":0.4,
 *      "delete": {
 *        "timestamp":1461063900000
 *      }
 *   }]
 * }
 * 
 * @apiParamExample {json} Request Example (Meter)
 * {
 *   "deviceKey":"dea05a8c-79ac-4f1c-ade0-b47c5af4b7fb",
 *   "type":"METER",
 *   "credentials": {
 *     username: "user@daiad.eu",
 *     password: "****"
 *   },
 *   "measurements":[{
 *     "volume":1923,
 *     "timestamp":1459478710000,
 *     "difference": 23
 *   }, {
 *     "volume":2000,
 *     "timestamp":1459488710000,
 *     "difference":77
 *   }]
 * }
 *
 * @apiSuccess {Boolean}  success           Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors            Array of <code>Error</code>.
 *
 * 
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
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
 *     code: "UserErrorCode.USERNANE_NOT_FOUND",
 *     description: "Account a9509da9-edf5-4838-acf4-8f1b73485d7a was not found."
 *   }],
 *   success: false
 * }
 */
 function storeDataByTime() { return; }

/**
 * @api {post} /v2/data/store Store (by index)
 * @apiVersion 0.0.2
 * @apiName DataStoreByIndex
 * @apiGroup Data
 * @apiPermission ROLE_USER, ROLE_ADMIN
 * 
 * @apiDescription Store data for Smart Water Meter or Amphiro devices. Accessing Amphiro devices requires <code>ROLE_USER</code> permission. Accessing Smart Water Meter devices requires <code>ROLE_ADMIN</code> permission. Data for meters and Amphiro devices are of type <code>AmphiroMeasurementCollection</code> and <code>WaterMeterMeasurementCollection</code> respectively. Both classes extend <code>DeviceMeasurementCollection</code>.<br/><br/>A historical session should not contain any measurements. If measurements are found for a historical session, an error will be returned. Moreover, all session identifiers and measurement indexes (for a specific parent session) must be unique. <b><span class="note">Amphiro session ordering is index based.</span></b>
 *
 * @apiParam (DeviceMeasurementCollection) {Object} credentials     User credentials
 * @apiParam (DeviceMeasurementCollection) {String} credentials.username        User name
 * @apiParam (DeviceMeasurementCollection) {String} credentials.password        User password
 * @apiParam (DeviceMeasurementCollection) {String} type            Device type. Valid values are <code>METER</code> and <code>AMPHIRO</code>.
 * @apiParam (DeviceMeasurementCollection) {String} deviceKey       Device unique id (UUID).
 * 
 * @apiParam (WaterMeterMeasurementCollection extends DeviceMeasurementCollection) {Object[]} measurements      Array of <code>WaterMeterMeasurement</code> representing meter measurements.
 * 
 * @apiParam (WaterMeterMeasurement) {Number}   timestamp   Measurement time stamp.
 * @apiParam (WaterMeterMeasurement) {Number}   volume      Volume.
 * @apiParam (WaterMeterMeasurement) {Number}   difference  Difference between the current and previous measurements.
 * 
 * @apiParam (AmphiroMeasurementCollection extends DeviceMeasurementCollection) {Object[]} sessions      Array of <code>AmphiroSession</code> representing Amphiro sessions.
 * @apiParam (AmphiroMeasurementCollection extends DeviceMeasurementCollection) {Object[]} measurements  Array of <code>AmphiroMeasurement</code> representing Amphiro detailed measurements.
 * 
 * @apiParam (AmphiroSession) {Number}       id                    Unique per device session id.
 * @apiParam (AmphiroSession) {Boolean}      history               Set to <code>false</code> for real time sessions; Otherwise <code>true</code>.
 * @apiParam (AmphiroSession) {Object[]}     properties            Session properties represented by an array of <code>KeyValuePair</code> objects.
 * @apiParam (AmphiroSession) {Number}       timestamp             Time stamp at which the mobile application has fetched the session data from the device over Bluetooth.
 * @apiParam (AmphiroSession) {Number}       volume                Total water volume.
 * @apiParam (AmphiroSession) {Number}       energy                Total energy.
 * @apiParam (AmphiroSession) {Number}       duration              Session duration.
 * @apiParam (AmphiroSession) {Number}       temperature           Average temperature.
 * @apiParam (AmphiroSession) {Number}       flow                  Average flow.
 * 
 * @apiParam (KeyValuePair) {String}         key                   Key.
 * @apiParam (KeyValuePair) {String}         value                 Value.
 * 
 * @apiParam (AmphiroMeasurement) {Number}   sessionId             Session id.
 * @apiParam (AmphiroMeasurement) {Number}   index                 Measurement index.
 * @apiParam (AmphiroMeasurement) {Boolean}  history               Set to <code>false</code> for real time sessions; Otherwise <code>true</code>.
 * @apiParam (AmphiroMeasurement) {Number}   timestamp             Time stamp.
 * @apiParam (AmphiroMeasurement) {Number}   temperature           Temperature.
 * @apiParam (AmphiroMeasurement) {Number}   volume                Total water volume.
 * @apiParam (AmphiroMeasurement) {Number}   energy                Total energy.
 * 
 * @apiParamExample {json} Request Example (Amphiro)
 * {
 *   "deviceKey": "4b6bb490-1c03-4c9d-b5d0-1dbb758bf71a",
 *   "type":"AMPHIRO",
 *   "credentials": {
 *     username: "user@daiad.eu",
 *     password: "****"
 *   },
 *   "sessions":[{
 *      "id":1,
 *      "timestamp":1461060000000,
 *      "duration":3,
 *      "history":false,
 *      "temperature":35.1,
 *      "volume":34.2,
 *      "energy":10,
 *      "flow":5,
 *      "properties":[]
 *   }],
 *      "measurements":[{
 *      "sessionId":1,
 *      "index":1,
 *      "history":false,
 *      "timestamp":1461060000000,
 *      "temperature":20,
 *      "volume":1.9,
 *      "energy":0.4
 *   }]
 * }
 * 
 * @apiParamExample {json} Request Example (Meter)
 * {
 *   "deviceKey":"dea05a8c-79ac-4f1c-ade0-b47c5af4b7fb",
 *   "type":"METER",
 *   "credentials": {
 *     username: "user@daiad.eu",
 *     password: "****"
 *   },
 *   "measurements":[{
 *     "volume":1923,
 *     "timestamp":1459478710000,
 *     "difference":20
 *   }, {
 *     "volume":2000,
 *     "timestamp":1459488710000,
 *     "difference":77
 *   }]
 * }
 *
 * @apiSuccess {Boolean}  success                 Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors                  Array of <code>Error</code>
 * 
 * 
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
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
 *     code: "UserErrorCode.USERNANE_NOT_FOUND",
 *     description: "Account a9509da9-edf5-4838-acf4-8f1b73485d7a was not found."
 *   }],
 *   success: false
 * }
 */
function storeDataByIndex() { return; }

