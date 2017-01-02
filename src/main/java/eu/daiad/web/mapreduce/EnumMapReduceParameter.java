package eu.daiad.web.mapreduce;


/**
 * Enumeration of MapReduce job parameters.
 */
public enum EnumMapReduceParameter {
    /**
     * Empty parameter.
     */
    EMPTY(null),
    /**
     * The job name.
     */
    JOB_NAME("mapreduce.job.name"),
    /**
     * Job jar name.
     */
    JAR_NAME("mapreduce.job.jar"),
    /**
     * Job main class.
     */
    MAIN_CLASS_NAME("mapreduce.job.main"),
    /**
     * Folder on HDFS for storing job temporary files.
     */
    TMP_HDFS_PATH("mapreduce.job.tmp.hdfs"),
    /**
     * Local path where external libraries are stored. During the job execution,
     * these libraries are copied to HDFS and added to the class path. External
     * libraries are used when the job class files are not packaged in a single
     * jar file.
     */
    LIB_LOCAL_PATH("mapreduce.job.lib.local"),
    /**
     * Local path with files that must be copied to HDFS before job execution.
     */
    FILE_LOCAL_PATH("mapreduce.job.files.local"),
    /**
     * HDFS path where job input files are copied before the job execution begins.
     */
    FILE_HDFS_PATH("mapreduce.job.files.hdfs"),
    /**
     * Local path with files to cache to YARN nodes before execution.
     */
    CACHE_LOCAL_PATH("mapreduce.job.cache.local"),
    /**
     * HDFS path with files to cache to YARN nodes before execution.
     */
    CACHE_HDFS_PATH("mapreduce.job.cache.hdfs"),
    /**
     * HDFS path.
     */
    HDFS_PATH("fs.defaultFS");

    private final String value;

    public String getValue() {
        return value;
    }

    private EnumMapReduceParameter(String value) {
        this.value = value;
    }

    public static EnumMapReduceParameter fromString(String value) {
        for (EnumMapReduceParameter item : EnumMapReduceParameter.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EMPTY;
    }
}
