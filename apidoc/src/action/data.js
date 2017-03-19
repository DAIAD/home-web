/**
 * @api {post} action/query Data query execution
 * @apiVersion 0.0.1
 * @apiName DataQuery
 * @apiGroup Data
 *
 * @apiDescription Executes a query for Smart Water Meter or/and Amphiro data. Users can apply consumer, spatial and time constraints. Currenlty spatial filters are evaluated against the selected user' smart water meter location. Population filters refer to single users, groups of users or utilities. Clusters are treated as a special case of a group filter where every cluster is replaced by a new population filter per cluster group. Time filter are either absolute or relative (sliding time window). Permissions are applied on population filters.
 *
 *
 * @apiParam {Ojbect} query                         Data query
 *
 * @apiParam {String}   [query.timezone]            Results are converted to the specified time zone. If not timezone is specified, the user's timezone is used. If user has not set her timezone, the timezone is set to <code>UTC</code>.
 *
 * @apiParam {Object}   query.time                  Time constraint.
 * @apiParam {String}   query.time.type             Time constraint type. Valid values are <code>ABSOLUTE</code> or <code>SLIDING</code>. An absolute time constraint requires the <code>start</code> and <code>end</code> parameters. A sliding time constraint requires <code>start</code>, <code>duration</code> and <code>durationTimeUnit</code> parameters.
 * @apiParam {String}   query.time.granularity      Defines the time interval over which the computation is applied. Valid values are <code>HOUR</code>, <code>DAY</code>, <code>WEEK</code>, <code>MONTH</code>, <code>YEAR</code>, <code>ALL</code>.<br/><br/>For instance, given an <code>ABSOLUTE</code> time constraint with granularity <code>DAY</code>, the computation will be applied for every day in this interval. The final result will have a single data point for every day in the time interval.
 * @apiParam {Number}   query.time.start            Start date as a UTC time stamp.
 * @apiParam {Number}   query.time.end              End date as a UTC time stamp. Required only by <code>ABSOLUTE</code> time constraints.
 * @apiParam {Number}   query.time.duration         Time interval duration. Required only by <code>SLIDING</code> time constraints. Negative values are allowed.
 * @apiParam {String}   query.time.durationTimeUnit Time interval duration unit. Required only by <code>SLIDING</code> time constraints.
 *
 * @apiParam {Object[]} query.population Consumer selection filter. An new data series is returned for every population filter. Population filter classes derive from <code>PopulationFilter</code>. Concrete implementations are <code>UserPopulationFilter</code>, <code>GroupPopulationFilter</code>, <code>ClusterPopulationFilter</code> and <code>UtilityPopulationFilter</code>.<br/></br>For additional details see specific types below.
 *
 * @apiParam {Object[]} query.spatial               Spatial constraints used for filtering consumers. Spatial constraint classes derive from <code>SpatialFilter</code>. Concrete implementations are <code>CustomSpatialFilter</code>, <code>AreaSpatialFilter</code>, <code>GroupSpatialFilter</code> and <code>ConstraintSpatialFilter</code>.<br/></br>For additional details see specific types below.
 *
 * @apiParam {String}   query.source Data source. Valid values are <code>BOTH</code>, <code>AMPHIRO</code> and <code>METER</code>. A data series is returned for every data source type.
 *
 * @apiParam {String}   query.metrics Operations applied. Valid values are <code>COUNT</code>, <code>SUM</code>, <code>MIN</code>, <code>MAX</code> or <code>AVERAGE</code>.
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
 * @apiParam (SpatialFilter) {String}   type  Filter type. Valid values are <code>CUSTOM</code>, <code>AREA</code>, <code>GROUP</code> and <code>CONSTRAINT</code>. For the first three types, one or more new population filters are created. In general if <code>M</code> population filters and <code>N</code> area filters are given, the response may contain up to <code>M*N</code> data series. The latter does not create a new population filter but filters all users in place.
 * <br/><br/><code>CUSTOM</code> selects all users whose locations are contained by the used defined geometry.
 * <br/><br/><code>AREA</code> selects all users whose locations are contained by the area geometry.
 * <br/><br/><code>GROUP</code> applies a filter to all areas of the group. It is equivelant of creating an <code>AREA</code> filter for every area in the group.
 * <br/><br/><code>CONSTRAINT</code> applies a spatial constraint to the location of all users.
 *
 * @apiParam (CustomSpatialFilter extends SpatialFilter)      {Object[]}  geometries  Array of <code>LabeledGeometry</code> objects.
 *
 * @apiParam (AreaSpatialFilter extends SpatialFilter)        {String[]}  areas       Array of <code>UUID</code> strings that identify specific areas. Each area generates a new population filter for each existing filter in <code>population</code> property.
 *
 * @apiParam (GroupSpatialFilter extends SpatialFilter)       {String}    group       Area group unique identifier (UUID). For each area in the group a new population filter for each existing filter in <code>population</code> property is created.
 *
 * @apiParam (ConstraintSpatialFilter extends SpatialFilter)  {String}    operation   Spatial operation. Valid values are <code>CONTAINS</code>, <code>INTERSECT</code> and <code>DISTANCE</code>.
 * @apiParam (ConstraintSpatialFilter extends SpatialFilter)  {Object}    geometry    Geometry expressed in GeoJSON format.
 *
 * @apiParam (ConstraintSpatialFilter extends SpatialFilter)  {Number}    [distance]  Optional distance parameter required only when <code>DISTANCE</code> is set for <code>operation</code> property.
 *
 * @apiParam (LabeledGeometry)      {String}  label     Area user friendly name e.g. neighborhood name.
 * @apiParam (LabeledGeometry)      {Object}  geometry  Area geometry expressed in GeoJSON format.
 *
 * @apiParamExample {json} Request Example
 *  {
 *    query: {
 *      time: {
 *        type : 'SLIDING',
 *        start: moment().valueOf(),
 *        duration: -60,
 *        durationTimeUnit: 'DAY',
 *        granularity: 'DAY'
 *      },
 *      population: [{
 *        type :'USER',
 *        label: 'user1@daiad.eu',
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
 *  }
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
 *       "label": "user1@daiad.eu",
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
 *       "label": "user1@daiad.eu",
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
 *           "label": "user@daiad.eu",
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
 *         "label": "user1@daiad.eu",
 *         "volume": {"SUM": 22}
 *        } ]
 *      }, {
 *        "type": "RANKING",
 *        "timestamp": 1459461600000,
 *        "users": [ {
 *          "key": "a203c47d-15ec-4cc1-b2aa-f2438042e619",
 *          "label": "user2@daiad.eu",
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
 * @api {post} action/data/meter/forecast Forecasting query execution
 * @apiVersion 0.0.1
 * @apiName ForecastQuery
 * @apiGroup Data
 *
 * @apiDescription Executes a forecating query for Smart Water Meter (SWM) data. Users can apply consumer, spatial and time constraints. Currenlty spatial filters are evaluated against the selected user' smart water meter location. Population filters refer to single users, groups of users or utilities. Clusters are treated as a special case of a group filter where every cluster is replaced by a new population filter per cluster segment. Time filters are either absolute or relative (sliding time window). Permissions are applied on population filters.
 *
 * @apiParam {Object}   query                       Data query
 *
 * @apiParam {String}   [query.timezone]            Results are converted to the specified time zone. If not timezone is specified, the user's timezone is used. If user has not set her timezone, the timezone is set to <code>UTC</code>.
 *
 * @apiParam {Object}   query.time                  Time constraint.
 * @apiParam {String}   query.time.type             Time constraint type. Valid values are <code>ABSOLUTE</code> or <code>SLIDING</code>. An absolute time constraint requires the <code>start</code> and <code>end</code> parameters. A sliding time constraint requires <code>start</code>, <code>duration</code> and <code>durationTimeUnit</code> parameters.
 * @apiParam {String}   query.time.granularity      Defines the time interval over which the computation is applied. Valid values are <code>HOUR</code>, <code>DAY</code>, <code>WEEK</code>, <code>MONTH</code>, <code>YEAR</code>, <code>ALL</code>.<br/><br/>For instance, given an <code>ABSOLUTE</code> time constraint with granularity <code>DAY</code>, the computation will be applied for every day in this interval. The final result will have a single data point for every day in the time interval.
 * @apiParam {Number}   query.time.start            Start date as a UTC time stamp.
 * @apiParam {Number}   query.time.end              End date as a UTC time stamp. Required only by <code>ABSOLUTE</code> time constraints.
 * @apiParam {Number}   query.time.duration         Time interval duration. Required only by <code>SLIDING</code> time constraints. Negative values are allowed.
 * @apiParam {String}   query.time.durationTimeUnit Time interval duration unit. Required only by <code>SLIDING</code> time constraints.
 *
 * @apiParam {Object[]} query.population            Consumer selection filter. An new data series is returned for every population filter. Population filter classes derive from <code>PopulationFilter</code>. Concrete implementations are <code>UserPopulationFilter</code>, <code>GroupPopulationFilter</code>, <code>ClusterPopulationFilter</code> and <code>UtilityPopulationFilter</code>.<br/></br>For additional details see specific types below.
 *
 * @apiParam {Object[]} query.spatial               Spatial constraints used for filtering consumers. Spatial constraint classes derive from <code>SpatialFilter</code>. Concrete implementations are <code>CustomSpatialFilter</code>, <code>AreaSpatialFilter</code>, <code>GroupSpatialFilter</code> and <code>ConstraintSpatialFilter</code>.<br/></br>For additional details see specific types below.
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
 * @apiParam (SpatialFilter) {String}   type  Filter type. Valid values are <code>CUSTOM</code>, <code>AREA</code>, <code>GROUP</code> and <code>CONSTRAINT</code>. For the first three types, one or more new population filters are created. In general if <code>M</code> population filters and <code>N</code> area filters are given, the response may contain up to <code>M*N</code> data series. The latter does not create a new population filter but filters all users in place.
 * <br/><br/><code>CUSTOM</code> selects all users whose locations are contained by the used defined geometry.
 * <br/><br/><code>AREA</code> selects all users whose locations are contained by the area geometry.
 * <br/><br/><code>GROUP</code> applies a filter to all areas of the group. It is equivelant of creating an <code>AREA</code> filter for every area in the group.
 * <br/><br/><code>CONSTRAINT</code> applies a spatial constraint to the location of all users.
 *
 * @apiParam (CustomSpatialFilter extends SpatialFilter)      {Object[]}  geometries  Array of <code>LabeledGeometry</code> objects.
 *
 * @apiParam (AreaSpatialFilter extends SpatialFilter)        {String[]}  areas       Array of <code>UUID</code> strings that identify specific areas. Each area generates a new population filter for each existing filter in <code>population</code> property.
 *
 * @apiParam (GroupSpatialFilter extends SpatialFilter)       {String}    group       Area group unique identifier (UUID). For each area in the group a new population filter for each existing filter in <code>population</code> property is created.
 *
 * @apiParam (ConstraintSpatialFilter extends SpatialFilter)  {String}    operation   Spatial operation. Valid values are <code>CONTAINS</code>, <code>INTERSECT</code> and <code>DISTANCE</code>.
 * @apiParam (ConstraintSpatialFilter extends SpatialFilter)  {Object}    geometry    Geometry expressed in GeoJSON format.
 *
 * @apiParam (ConstraintSpatialFilter extends SpatialFilter)  {Number}    [distance]  Optional distance parameter required only when <code>DISTANCE</code> is set for <code>operation</code> property.
 *
 * @apiParam (LabeledGeometry)      {String}  label     Area user friendly name e.g. neighborhood name.
 * @apiParam (LabeledGeometry)      {Object}  geometry  Area geometry expressed in GeoJSON format.
 *
 * @apiParamExample {json} Request Example
 * {
 *   query: {
 *     time: {
 *       type : 'SLIDING',
 *       start: moment().valueOf(),
 *       duration: -60,
 *       durationTimeUnit: 'DAY',
 *       granularity: 'DAY'
 *     },
 *     population: [{
 *       type :'USER',
 *       label: 'user1@daiad.eu',
 *       users: ['63078a88-f75a-4c5e-8d75-b4472ba456bb']
 *     }, {
 *       type :'CLUSTER',
 *       label: 'Income',
 *       clusterType: 'INCOME'
 *     }, {
 *       type :'UTILITY',
 *       label: 'Alicante (all)',
 *       utility: '2b48083d-6f05-488f-9f9b-99607a93c6c3'
 *     }, {
 *       type :'UTILITY',
 *       label: 'Alicante (top 2)',
 *       utility: '2b48083d-6f05-488f-9f9b-99607a93c6c3',
 *       ranking: {
 *         type: 'TOP',
 *         limit: 2
 *       }
 *     }],
 *     spatial: {
 *       type: 'CONSTRAINT',
 *       operation: 'CONTAINS',
 *       geometry: {
 *         'type': 'Polygon',
 *         'coordinates': [
 *           [
 *             [
 *                 -0.525970458984375,
 *                 38.329537722849636
 *             ], [
 *                 -0.5233955383300781,
 *                 38.36386812314455
 *             ], [
 *                 -0.4821968078613281,
 *                 38.37651914591569
 *             ], [
 *                 -0.4440879821777344,
 *                 38.33963658855894
 *             ], [
 *                 -0.46966552734375,
 *                 38.31647443592999
 *             ], [
 *                 -0.5089759826660156,
 *                  38.313511301083466
 *             ], [
 *                 -0.525970458984375,
 *                 38.329537722849636
 *             ]
 *           ]
 *         ]
 *       }
 *     }
 *   }
 *
 * @apiSuccess {Boolean}      success                   <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]}     errors                    Array of error messages.
 * @apiSuccess {String}       timezone                  Timezone of the result e.g. <code>Europe/Athens</code>.
 * @apiSuccess {Ojbect}       areas                     A map with <code>Number</code> keys and <code>LabeledGeometry</code> values representing all areas contained in the result.
 * @apiSuccess {Object[]}     meters                    Collection of <code>DataSeries</code> series with smart water meter data.
 *
 * @apiSuccess (LabeledGeometry)  {String}  label       Area user friendly name e.g. neighborhood name.
 * @apiSuccess (LabeledGeometry)  {Object}  geometry    Area geometry expressed in GeoJSON format.
 *
 * @apiSuccess (DataSeries) {String} label              Result user friendly name as declared in the query.
 * @apiSuccess (DataSeries) {Number} [areaId]           Key to the <code>areas</code> map if this data serie is associated to an area.
 * @apiSuccess (DataSeries) {Number} population         Number of unique users found. This field is the number of users that contributed data to the final result. For instance a group may have 100 members but the <code>population</code> value may be less than 100. This may occur if a spatial field has been applied.
 * @apiSuccess (DataSeries) {Object[]}  points          Data points. All data point classes dervice from <code>DataPoint</code>. A response may contain instances of <code>MeterDataPoint</code> or <code>RankingDataPoint</code>.
 *
 * @apiSuccess (DataPoint)  {String}    type            Data point type. Valid values are <code>METER</code> and <code>RANKING</code>. Based on the type, the implementing class is <code>MeterDataPoint</code> and <code>RankingDataPoint</code> respectively.<br/><br/>Instances of <code>MeterDataPoint</code> are returned for meter results.<br/><br/>Instances of <code>RankingDataPoint</code> are returned in any series for which the <code>ranking</code> property is set for the corresponding <code>population</code> filter.
 * @apiSuccess (DataPoint) {Number}     timestamp       Time stamp.
 *
 * @apiSuccess (MeterDataPoint extends DataPoint)   {Object}    volume          Volume data.
 * @apiSuccess (MeterDataPoint extends DataPoint)   {Object}    [volume.SUM]    Total water consumption.
 * @apiSuccess (MeterDataPoint extends DataPoint)   {Object}    [volume.COUNT]  Number of unique users that contributed to the result.
 *
 * @apiSuccess (RankingDataPoint extends DataPoint) {Object[]}  users           Unique users expressed as objects of type <code>MeterUserDataPoint</code> which derives from <code>UserDataPoint</code>.
 *
 * @apiSuccess (UserDataPoint) {String} key     Unique user key (UUID).
 * @apiSuccess (UserDataPoint) {String} label   User friendly name. The username account is returned by default.
 *
 * @apiSuccess (MeterUserDataPoint extends UserDataPoint) {Object} volume Volume data.
 * @apiSuccess (MeterUserDataPoint extends UserDataPoint) {Number} volume.SUM Total water consumption.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "timezone": "Europe/Madrid",
 *   "areas": null,
 *   "meters":[
 *     {
 *       "label": "user1@daiad.eu",
 *       "population": 1,
 *       "areaId": null,
 *       "points": [
 *          {
 *             "type": "METER",
 *             "timestamp": 1462831200000,
 *             "volume": {
 *                "SUM": 84,
 *                "COUNT": 1
 *             }
 *          }, {
 *             "type": "METER",
 *             "timestamp": 1462744800000,
 *             "volume": {
 *                "SUM": 2,
 *                "COUNT": 1
 *             }
 *          }
 *       ]
 *    }, {
 *       "label": "Alicante",
 *       "population": 15,
 *       "areaId": null,
 *       "points": [
 *          {
 *             "type": "METER",
 *             "timestamp": 1462831200000,
 *             "volume": {
 *                "SUM": 440,
 *                "COUNT": 10
 *             }
 *          }, {
 *             "type": "METER",
 *             "timestamp": 1462572000000,
 *             "volume": {
 *                "SUM": 180,
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
 *   "areas" : null,
 *   "meters": [ {
 *     "label": "Alicante",
 *     "population": 15,
 *     "areaId": null,
 *     "points": [ {
 *       "type": "RANKING",
 *       "timestamp": 1462053600000,
 *       "users": [ {
 *         "key": "92c74655-a606-4f29-aa56-cc478aa730fb",
 *         "label": "user 1",
 *         "volume": {"SUM": 22}
 *        } ]
 *      }, {
 *        "type": "RANKING",
 *        "timestamp": 1459461600000,
 *        "users": [ {
 *          "key": "a203c47d-15ec-4cc1-b2aa-f2438042e619",
 *          "label": "user 2",
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
function forecast() { return; }


/**
 * @api {post} action/data/session/member Assign members
 * @apiVersion 0.0.2
 * @apiName AssignMemberToSession
 * @apiGroup Data
 * @apiPermission ROLE_USER
 *
 * @apiDescription Assigns household members to shower sessions.
 *
 * @apiParam (MemberAssignmentRequest) {Object[]} assignments              Array of <code>Assignment</code> objects.
 *
 * @apiParam (Assignment) {String}   deviceKey    Device unique key (UUID).
 * @apiParam (Assignment) {Number}   sessionId    Session id.
 * @apiParam (Assignment) {Number}   memberIndex  Household member unique index.
 * @apiParam (Assignment) {Number}   timestamp    Update operation time stamp.
 *
 * @apiParamExample {json} Request Example
 * {
 *   "assignments":[{
 *     "deviceKey": "4b6bb490-1c03-4c9d-b5d0-1dbb758bf71a",
 *     "sessionId":2,
 *     "memberIndex":14,
 *     "timestamp" : 1461060000000
 *   }]
 * }
 *
 * @apiSuccess {Boolean}  success                 Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors                  Array of <code>Error</code> objects.
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
 *     code: "DataErrorCode.SESSION_NOT_FOUND",
 *     description: "Session 4 was not found."
 *   }],
 *   success: false
 * }
 */
function assignMemberToSession() { return; }

/**
 * @api {post} action/data/session/ignore Ignore showers
 * @apiVersion 0.0.2
 * @apiName IgnoreSession
 * @apiGroup Data
 * @apiPermission ROLE_USER
 *
 * @apiDescription Mark an amphiro b1 session as not being a shower.
 *
 * @apiParam (IgnoreShowerRequest) {Object[]} sessions                 Array of <code>Session</code> objects.
 *
 * @apiParam (Session) {String}   deviceKey    Device unique key (UUID).
 * @apiParam (Session) {Number}   sessionId    Session id.
 * @apiParam (Session) {Number}   timestamp    Update operation time stamp.
 *
 * @apiParamExample {json} Request Example
 * {
 *   "sessions":[{
 *     "deviceKey": "4b6bb490-1c03-4c9d-b5d0-1dbb758bf71a",
 *     "sessionId":2,
 *     "timestamp" : 1461060000000
 *   }]
 * }
 *
 * @apiSuccess {Boolean}  success                 Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors                  Array of <code>Error</code> objects.
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
 *     code: "DataErrorCode.SESSION_NOT_FOUND",
 *     description: "Session 4 was not found."
 *   }],
 *   success: false
 * }
 */
function ignoreSession() { return; }
