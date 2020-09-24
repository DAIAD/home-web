package eu.daiad.scheduler.flink;

public enum EnumFlinkParameter {
    /**
     * Job name.
     */
    JOB_NAME("job.name"),
    /**
     * Job script.
     */
    JOB_SCRIPT("job.script"),
    /**
     * Job script arguments
     */
    JOB_SCRIPT_ARGS("job.script.args"),
    /**
     * HDFS path where job input files are copied before the job execution begins.
     */
    WORKING_DIRECTORY("working.directory");

    private final String value;

    public String getValue() {
        return value;
    }

    private EnumFlinkParameter(String value) {
        this.value = value;
    }

}
