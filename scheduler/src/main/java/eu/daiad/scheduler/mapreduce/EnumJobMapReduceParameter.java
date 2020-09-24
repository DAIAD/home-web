package eu.daiad.scheduler.mapreduce;

/**
 * Enumeration of custom MapReduce job parameters.
 */
public enum EnumJobMapReduceParameter {
    /**
     * Not supported parameter.
     */
    NOT_SUPPORTED(""),
    /**
     * The job name.
     */
    JOB_NAME("daiad.mapreduce.job.name"),
    /**
     * Job jar name.
     */
    JAR_NAME("daiad.mapreduce.job.jar"),
    /**
     * Job main class.
     */
    MAIN_CLASS_NAME("daiad.mapreduce.job.main"),
    /**
     * Local working directory.
     */
    LOCAL_TMP_PATH("daiad.mapreduce.job.local.tmp"),
    /**
     * Folder on HDFS for storing job temporary files.
     */
    HDFS_TMP_PATH("daiad.mapreduce.job.hdfs.tmp"),
    /**
     * Local path where external libraries are stored. During the job execution,
     * these libraries are copied to HDFS and added to the class path. External
     * libraries are used when the job class files are not packaged in a single
     * jar file.
     */
    LOCAL_LIB_PATH("daiad.mapreduce.job.local.lib"),
    /**
     * Local path with files that must be copied to HDFS before job execution.
     */
    LOCAL_FILE_PATH("daiad.mapreduce.job.local.file"),
    /**
     * Local path with files to cache to YARN nodes before execution.
     */
    LOCAL_CACHE_PATH("daiad.mapreduce.job.local.cache"),
    /**
     * HDFS folder with external libraries required for the job execution.
     */
    HDFS_LIB_PATH("daiad.mapreduce.job.hdfs.lib"),
    /**
     * HDFS path where job input files are copied before the job execution
     * begins.
     */
    HDFS_FILE_PATH("daiad.mapreduce.job.hdfs.file"),
    /**
     * HDFS path with files to cache to YARN nodes before execution.
     */
    HDFS_CACHE_PATH("daiad.mapreduce.job.hdfs.cache");

    private final String value;

    public String getValue() {
        return value;
    }

    private EnumJobMapReduceParameter(String value) {
        this.value = value;
    }

    public static EnumJobMapReduceParameter fromString(String value) {
        for (EnumJobMapReduceParameter item : EnumJobMapReduceParameter.values()) {
            if (item.getValue().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return NOT_SUPPORTED;
    }
}