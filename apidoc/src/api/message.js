/**
 * @apiDefine Credentials User credentials used for authentication
 * @apiParam {String} username User name
 * @apiParam {String} password User password
 */

/**
 * @api {post} /v1/message Load messages
 * @apiVersion 0.0.1
 * @apiName Message
 * @apiGroup Message
 * @apiPermission ROLE_USER
 *
 * @apiDescription Loads all available messages. All messages returned derive from  <code>Message</code>.
 *
 * @apiParam                            {Object}   credentials            User credentials used for authentication.
 * @apiParam                            {String}   credentials.username   User name
 * @apiParam                            {String}   credentials.password   User password
 * @apiParam                            {String}   [locale]            The target locale as a ISO-639 language code (e.g. <code>el</code>). If not supplied, the locale of current authenticated user will be used.
 * @apiParam                            {Object[]} messages               Array of <code>Options</code> objects. Each object represents a request for a specific type of messages. If more than one option objects are found for a single message type, the first overrides the others.
 *
 *
 * @apiParam (Options)   {String}   type                   Message type. Valid values are <code>ALERT</code>, <code>RECOMMENDATION</code>, <code>TIP</code> and <code>ANNOUNCEMENT</code>. This parameter is not case sensitive.
 * @apiParam (Options)   {Number}   [minMessageId]         Filter messages by a minimum message id. This option is most useful to the <code>MOBILE</code> client if only the messages after a specific message are required.
 * @apiParam (Options)   {Object}   [pagination]             Provide data pagination options. 
 * @apiParam (Options)   {Number}   [pagination.offset]      Start fetching from this index. If not set, data paging starts from the first record.
 * @apiParam (Options)   {Number}   [pagination.size]        The number of records to return. If not set, all records are returned.
 * @apiParam (Options)   {Boolean}  [pagination.ascending]   Control ordering of returned records: <code>true</code> for ordering ascending on message id, otherwise <code>false</code>. Sorting is performed on the message id field. Sorting on message id field is equivalent to sorting over creation date. Default value is <code>true</code>.
 *
 *
 * @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     username: "user@daiad.eu",
 *     password: "****"
 *   },
 *   "messages": [
 *      {
 *         "type": "ALERT",
 *         "minMessageId": 11233,
 *         "pagination": {
 *             "offset": 10,
 *             "size": 5,
 *             "ascending": false
 *         }
 *      },
 *      {
 *         "type": "RECOMMENDATION",
 *         "minMessageId": 932,
 *      }
 *   ]
 * }
 *
 *
 *
 * @apiSuccess {Boolean}  success                  <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors                   Empty array of error messages.
 * @apiSuccess {Object[]} alerts                   Array of <code>Alert</code> objects.
 * @apiSuccess {Object[]} recommendations          Array of <code>Recommendation</code> objects.
 * @apiSuccess {Object[]} tips                     Array of <code>Tip</code> objects.
 * @apiSuccess {Object[]} announcements            Array of <code>Announcement</code> objects.
 * @apiSuccess {Number}   totalAlerts              Total number of alerts.
 * @apiSuccess {Number}   totalRecommendations     Total number of recommendations.
 * @apiSuccess {Number}   totalTips                Total number of tips.
 * @apiSuccess {Number}   totalAnnouncements       Total number of announcements.
 *
 *
 * @apiSuccess (Message) {String}  type      Message type. One of <code>ALERT</code>, <code>TIP</code>, <code>RECOMMENDATION</code> and <code>ANNOUNCEMENT</code>.
 * @apiSuccess (Message) {Number}  id        Message id.
 * @apiSuccess (Message) {String}  title        Title
 * @apiSuccess (Message) {Number}  createdOn         Date created timestamp.
 * @apiSuccess (Message) {Number}  acknowledgedOn    Date acknowledged timestamp. This is the date the user has marked the message as read from the client application.
 *
 * @apiSuccess (Alert extends Message) {String} alertType     Alert type. One of:
 *   <br/><code>WATER_LEAK</code> 
 *   <br/><code>SHOWER_ON</code> 
 *   <br/><code>WATER_FIXTURES</code> 
 *   <br/><code>UNUSUAL_ACTIVITY</code> 
 *   <br/><code>WATER_QUALITY</code> 
 *   <br/><code>HIGH_TEMPERATURE</code> 
 *   <br/><code>NEAR_DAILY_BUDGET</code> 
 *   <br/><code>NEAR_WEEKLY_BUDGET</code> 
 *   <br/><code>REACHED_DAILY_BUDGET</code> 
 *   <br/><code>REACHED_WEEKLY_BUDGET</code> 
 *   <br/><code>CHAMPION</code> 
 *   <br/><code>TOO_MUCH_WATER</code> 
 *   <br/><code>TOO_MUCH_ENERGY</code> 
 *   <br/><code>REDUCED_WATER_USE</code> 
 *   <br/><code>WATER_EFFICIENCY_LEADER</code> 
 *   <br/><code>KEEP_UP_SAVING_WATER</code> 
 *   <br/><code>GOOD_JOB_MONTHLY</code> 
 *   <br/><code>LITERS_ALREADY_SAVED</code> 
 *   <br/><code>TOP_25_PERCENT_OF_SAVERS</code> 
 *   <br/><code>TOP_10_PERCENT_OF_SAVERS</code> 
 * @apiSuccess (Alert extends Message) {String} alertCode      The code(s) for this message as defined in DAIAD deliverable 3.2.2. If this <code>alertType</code> maps to more than one codes, they are separated by <code>|</code>.
 * @apiSuccess (Alert extends Message) {Number} priority       Message priority.
 * @apiSuccess (Alert extends Message) {String} description    Long description.
 * @apiSuccess (Alert extends Message) {String} link      A resource (e.g. image) link.
 * @apiSuccess (Alert extends Message) {Number} refDate   The reference date for this message. 
 *
 *
 * @apiSuccess (Tip extends Message) {Number}  index              Message index.
 * @apiSuccess (Tip extends Message) {String}  description        Long description.
 * @apiSuccess (Tip extends Message) {String}  categoryName       Category.
 * @apiSuccess (Tip extends Message) {String}  imageEncoded       Image encoded to base64.
 * @apiSuccess (Tip extends Message) {String}  imageLink          Image resource link.
 * @apiSuccess (Tip extends Message) {String}  imageMimeType      Image MIME type.
 * @apiSuccess (Tip extends Message) {String}  prompt             Prompt message.
 * @apiSuccess (Tip extends Message) {String}  externalLink       External link.
 * @apiSuccess (Tip extends Message) {String}  source             Message source.
 * @apiSuccess (Tip extends Message) {Number}  modifiedOn         Date modified timestamp.
 * @apiSuccess (Tip extends Message) {Boolean} active             <code>true</code> if message is acrive; Otherwise <code>false</code>.
 *
 * @apiSuccess (Recommendation extends Message) {String} recommendationType  Recommendation type. One of:
 *   <br/><code>LESS_SHOWER_TIME</code> 
 *   <br/><code>LOWER_TEMPERATURE</code> 
 *   <br/><code>LOWER_FLOW</code> 
 *   <br/><code>CHANGE_SHOWERHEAD</code> 
 *   <br/><code>CHANGE_SHAMPOO</code> 
 *   <br/><code>REDUCE_FLOW_WHEN_NOT_NEEDED</code> 
 *   <br/><code>INSIGHT_A1</code> 
 *   <br/><code>INSIGHT_A2</code> 
 *   <br/><code>INSIGHT_A3</code> 
 *   <br/><code>INSIGHT_A4</code> 
 *   <br/><code>INSIGHT_B1</code> 
 *   <br/><code>INSIGHT_B2</code> 
 *   <br/><code>INSIGHT_B3</code> 
 *   <br/><code>INSIGHT_B4</code> 
 *   <br/><code>INSIGHT_B5</code> 
 * @apiSuccess (Recommendation extends Message) {String} recommendationCode      The code(s) for this message as defined in DAIAD deliverable 3.2.2. If this <code>recommendationType</code> maps to more than one codes, they are separated by <code>|</code>. 
 * @apiSuccess (Recommendation extends Message) {Number} priority        Message priority.
 * @apiSuccess (Recommendation extends Message) {String} description     Long description.
 * @apiSuccess (Recommendation extends Message) {String} imageLink       A resource link.
 * @apiSuccess (Recommendation extends Message) {Number} refDate         The reference date for this message. 
 *
 *
 * @apiSuccess (Announcement extends Message) {Number} priority         Message priority.
 * @apiSuccess (Announcement extends Message) {String} content          Announcement details.
 * @apiSuccess (Announcement extends Message) {String} link             Image resource link.
 *
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 *  {
 *    "errors": [],
 *    "alerts": [
 *      {
 *        "priority": 5,
 *        "alertType": "TOO_MUCH_WATER",
 *        "alertTemplate": "TOO_MUCH_WATER_METER",
 *        "alertCode": "A17|A18",
 *        "description": "You are using twice the amount of water compared to city average. You could save up to 57,949 liters. Want to learn how?",
 *        "link": null,
 *        "body": "You are using too much water",
 *        "type": "ALERT",
 *        "id": 842,
 *        "locale": "en",
 *        "title": "You are using too much water",
 *        "refDate": 1479160800000,
 *        "acknowledgedOn": 1487756290000,
 *        "createdOn": 1487756280417
 *      }
 *    ],
 *    "totalAlerts": 1,
 *    "recommendations": [
 *      {
 *        "priority": 5,
 *        "recommendationType": "INSIGHT_A3",
 *        "recommendationTemplate": "INSIGHT_A3_MORNING_CONSUMPTION_DECR",
 *        "recommendationCode": "IA3",
 *        "description": "8.0lt vs. the average 22.2lt",
 *        "link": null,
 *        "body": "63% decrease in morning consumption",
 *        "id": 5018,
 *        "locale": "en",
 *        "title": "63% decrease in morning consumption",
 *        "refDate": 1479160800000,
 *        "acknowledgedOn": 1487756290000,
 *        "createdOn": 1487756527611,
 *        "type": "RECOMMENDATION"
 *      },
 *      {
 *        "priority": 5,
 *        "recommendationType": "INSIGHT_B1",
 *        "recommendationTemplate": "INSIGHT_B1_MONTHLY_CONSUMPTION_INCR",
 *        "recommendationCode": "IB1",
 *        "description": "8.0lt vs. the average 22.2lt",
 *        "description": "6598.0lt vs. the average 2652.5lt",
 *        "link": null,
 *        "body": "148% more than your monthly average",
 *        "id": 5029,
 *        "locale": "en",
 *        "title": "148% more than your monthly average",
 *        "refDate": 1479160800000,
 *        "acknowledgedOn": null,
 *        "createdOn": 1487756580059,
 *        "type": "RECOMMENDATION"
 *      },
 *      {
 *        "priority": 5,
 *        "recommendationType": "INSIGHT_B2",
 *        "recommendationTemplate": "INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_INCR",
 *        "recommendationCode": "IB2",
 *        "description": "8.0lt vs. the average 22.2lt",
 *        "description": "6598.0lt vs. 2535.0lt",
 *        "link": null,
 *        "body": "160% more than previous month",
 *        "id": 5034,
 *        "locale": "en",
 *        "title": "160% more than previous month",
 *        "refDate": 1479160800000,
 *        "acknowledgedOn": null,
 *        "createdOn": 1487756630246,
 *        "type": "RECOMMENDATION"
 *      },
 *    ],
 *    "totalRecommendations": 8,
 *    "tips": [],
 *    "totalTips": 0,
 *    "announcements": [],
 *    "totalAnnouncements": 0,
 *    "success": true
 *  }
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
function getMessages() { return; }



/**
 * @api {post} /v1/message/acknowledge Acknowledge messages
 * @apiVersion 0.0.1
 * @apiName MessageAcknowledge
 * @apiGroup Message
 * @apiPermission ROLE_USER
 *
 * @apiDescription Marks one or more messages as acknowledged.
 *
 * @apiParam                            {Object}   credentials            User credentials used for authentication.
 * @apiParam                            {String}   credentials.username   User name
 * @apiParam                            {String}   credentials.password   User password
 * @apiParam                            {Object[]} messages               Array of <code>MessageAcknowledgement</code>.
 *
 * @apiParam (MessageAcknowledgement)   {String}   type                   Message type. Valid values are <code>ALERT</code>, <code>TIP</code>, <code>RECOMMENDATION</code> and <code>ANNOUNCEMENT</code>. This parameter is not case sensitive.
 * @apiParam (MessageAcknowledgement)   {Number}   id                     Message id. This id is unique per message type.
 * @apiParam (MessageAcknowledgement)   {Number}   timestamp              A timestamp the message was read by the user i.e. the timestamp at the mobile device.
 *
 * @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     username: "user@daiad.eu",
 *     password: "****"
 *   },
 *   "messages": [{
 *     "id": 1625,
 *     "timestamp": 1463667895000,
 *     "type": "ALERT"
 *   }, {
 *     "id": 1797,
 *     "timestamp": 1463667895000,
 *     "type": "ALERT"
 *   }]
 * }
 *
 * @apiSuccess {Boolean}  success                  <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors                   Empty array of error messages.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   success: true
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
 *     code: "SharedErrorCode.UNKNOWN",
 *     description: "Unknown message acknowledgement type."
 *   }],
 *   success: false
 * }
 */
function acknowledgeMessages() { return; }
