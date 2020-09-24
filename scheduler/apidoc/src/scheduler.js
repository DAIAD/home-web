/**
 * @api {get} /v1/scheduler/jobs Get jobs
 * @apiVersion 1.0.0
 * @apiName GetJobs
 * @apiGroup Scheduler
 *
 * @apiDescription Get all registered jobs
 *
 * @apiParamExample {json} Request Example
 * GET /api/v1/scheduler/jobs HTTP/1.1
 *
 * @apiSuccess {Boolean}  success                  <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors                   Empty array of error messages.
 *
 * @apiSuccess {Boolean}  success             <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors              Array of <code>Error</code> objects.
 * @apiSuccess {Object[]} jobs                Array of <code>Job</code> objects.
 *
 * @apiSuccess (Error) {String} code          Unique error code.
 * @apiSuccess (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 *
 * @apiSuccess (Error) {number}     id                          Unique job id
 * @apiSuccess (Error) {String}     category                    Job category</br>
 * <code>MAINTENANCE</code></br>
 * <code>ETL</code></br>
 * <code>ANALYSIS</code></br>
 * <code>FORECASTING</code>
 * 
 * @apiSuccess (Error) {String}     container                   Job execution environment</br>
 * <code>RUNTIME</code></br>
 * <code>HADOOP</code></br>
 * <code>FLINK</code>
 * 
 * @apiSuccess (Error) {String}     name                        Job short name
 * @apiSuccess (Error) {String}     description                 Job detailed description
 * @apiSuccess (Error) {number}     lastExecution               Last execution timestamp
 * @apiSuccess (Error) {number}     lastExecutionDuration       Last execution duration (milliseconds)
 * @apiSuccess (Error) {String}     lastExecutionExitCode       Last execution exit code</br>
 * <code>UNKNOWN</code></br>
 * <code>EXECUTING</code></br>
 * <code>COMPLETED</code></br>
 * <code>NOOP</code></br>
 * <code>FAILED</code></br>
 * <code>STOPPED</code>
 * 
 * @apiSuccess (Error) {String}     lastExecutionExitMessage    Last execution exit message. <code>null</code> if no messages exist
 * @apiSuccess (Error) {number}     nextExecution               Next execution timestamp
 * @apiSuccess (Error) {boolean}    enabled                     <code>true</code> if job is scheduled for execution
 * @apiSuccess (Schedule) {Object}  schedule                    Instance of <code>Schedule</code> object
 * @apiSuccess (Error) {number}     progress                    Execution progress as percent or <code>null</code> if job is not running
 * @apiSuccess (Error) {boolean}    running                     <code>true</code> if job is running
 * @apiSuccess (Error) {boolean}    visible                     <code>true</code> if job is visible in the UI
 * 
 * @apiSuccess (Schedule) {String} cronExpression               CRON expression if job is executed using a CRON expression
 * @apiSuccess (Schedule) {number} period                       Number of seconds if job is executed periodically
 * @apiSuccess (Schedule) {String} type                         Time interval type</br>
 * <code>PERIOD</code></br>
 * <code>CRON</code>
 * 
 * @apiSuccessExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *  "errors": [],
 *  "jobs": [
 *   {
 *             "id": 22,
 *             "category": "ETL",
 *             "container": "RUNTIME",
 *             "name": "SFTP",
 *             "description": "Meter Data SFTP Loader",
 *             "lastExecution": 1600676968035,
 *             "lastExecutionDuration": 102683,
 *             "lastExecutionExitCode": "COMPLETED",
 *             "lastExecutionExitMessage": "",
 *             "nextExecution": null,
 *             "enabled": false,
 *             "schedule": {
 *                 "cronExpression": "0 0 *\/4 * * *",
 *                 "period": null,
 *                 "type": "CRON"
 *             },
 *             "progress": null,
 *             "running": false,
 *             "visible": true
 *         }
 *     ],
 *     "success": true
 * }
 */
function getJobs() { return; }
