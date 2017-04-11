package eu.daiad.web.job.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.domain.application.AreaGroupMemberEntity;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.SchedulerErrorCode;
import eu.daiad.web.model.group.EnumGroupType;
import eu.daiad.web.model.group.Group;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IGroupRepository;
import eu.daiad.web.repository.application.ISpatialRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IUtilityRepository;

/**
 * Task for exporting the members of all groups to a CSV file.
 */
@Component
public class ExportGroupMemberToFileTask extends BaseTask implements StoppableTasklet {

    /**
     * Delimiter character used for separating values in output file.
     */
    private static final char DELIMITER = ';';

    /**
     * Record type for a single area.
     */
    private static final String RECORD_TYPE_AREA = "AREA";

    /**
     * Repository for accessing utility data.
     */
    @Autowired
    private IUtilityRepository utilityRepository;

    /**
     * Repository for accessing group data.
     */
    @Autowired
    private IGroupRepository groupRepository;

    /**
     * Repository for accessing spatial data.
     */
    @Autowired
    private ISpatialRepository spatialRepository;

    /**
     * Repository for accessing user data.
     */
    @Autowired
    protected IUserRepository userRepository;

    /**
     * Repository for accessing device (smart water meter) data.
     */
    @Autowired
    protected IDeviceRepository deviceRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        CSVPrinter groupPrinter = null;
        CSVPrinter userPrinter = null;

        try {
            // Get all step parameters
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            // Ensure that the working directory exists
            String workingDirectory = parameters.get(EnumInParameter.WORKING_DIRECTORY.getValue());
            ensureDirectory(new File(workingDirectory));

            // Ensure that the output filename parameters are set and construct the output filename.
            ensureParameter(parameters, EnumInParameter.OUTPUT_FILENAME_GROUPS.getValue());
            String groupFilename = FilenameUtils.concat(workingDirectory, parameters.get(EnumInParameter.OUTPUT_FILENAME_GROUPS.getValue()));

            ensureParameter(parameters, EnumInParameter.OUTPUT_FILENAME_USERS.getValue());
            String userFilename = FilenameUtils.concat(workingDirectory, parameters.get(EnumInParameter.OUTPUT_FILENAME_USERS.getValue()));

            ensureFile(groupFilename);
            ensureFile(userFilename);

            CSVFormat format = CSVFormat.RFC4180.withDelimiter(DELIMITER);

            groupPrinter = new CSVPrinter(
                             new BufferedWriter(
                               new OutputStreamWriter(
                                 new FileOutputStream(groupFilename, true),
                                 Charset.forName("UTF-8").newEncoder()
                               )
                             ),
                             format);

            userPrinter = new CSVPrinter(
                            new BufferedWriter(
                              new OutputStreamWriter(
                                new FileOutputStream(userFilename, true),
                                Charset.forName("UTF-8").newEncoder()
                              )
                            ),
                            format);

            // Optionally filter utility data
            Integer utilityId = this.getInteger(parameters, EnumInParameter.UTILITY_ID.getValue(), false);

            // Export data
            exportUtilities(groupPrinter, userPrinter, utilityId);
        } catch (Throwable t) {
            throw wrapApplicationException(t, SchedulerErrorCode.SCHEDULER_JOB_STEP_FAILED).set("step", chunkContext.getStepContext().getStepName());
        } finally {
            if (groupPrinter != null) {
                IOUtils.closeQuietly(groupPrinter);
            }
            if (userPrinter != null) {
                IOUtils.closeQuietly(userPrinter);
            }
        }

        return RepeatStatus.FINISHED;
    }

    private void exportUtilities(CSVPrinter groupPrinter, CSVPrinter userPrinter, Integer utilityId) throws IOException {
        for (UtilityInfo utility : utilityRepository.getUtilities()) {
            if ((utilityId != null) && (utility.getId() != utilityId)) {
                continue;
            }

            List<AreaGroupMemberEntity> areas = spatialRepository.getAreasByUtilityId(utility.getKey());

            String timezone = utility.getTimezone();
            if (StringUtils.isBlank(timezone)) {
                continue;
            }

            // Export all utility groups and members
            List<UUID> members = utilityRepository.getMembers(utility.getKey());

            for (UUID userKey : members) {
                List<Device> devices = deviceRepository.getUserDevices(userKey, EnumDeviceType.METER);
                if (devices.isEmpty()) {
                    continue;
                }
                AuthenticatedUser user = userRepository.getUserByKey(userKey);

                for (Device device : devices) {
                    WaterMeterDevice meter = (WaterMeterDevice) device;

                    // Export user
                    printUserRow(userPrinter,user, meter);

                    printGroupRow(groupPrinter, EnumGroupType.UTILITY.toString(), utility.getKey(), meter.getSerial(), timezone);

                    // Export utility and area
                    if (meter.getLocation() != null) {
                        for (AreaGroupMemberEntity area : areas) {
                            if (area.getGeometry().contains(meter.getLocation())) {
                                printGroupRow(groupPrinter, RECORD_TYPE_AREA, utility.getKey(), meter.getSerial(), timezone, area.getKey());
                            }
                        }
                    }
                }
            }

            // Export all groups
            for (Group group : groupRepository.getAll(utility.getKey())) {
                switch(group.getType()) {
                    case UTILITY: case CLUSTER:
                        // (a) a utility is already exported and
                        // (b) a cluster is always expanded to segments (it is equivalent to the utility) and
                        break;
                    case SEGMENT: case COMMONS: case SET:
                        exportGroups(groupPrinter, group, timezone, areas);
                        break;
                    default:
                        throw new IllegalArgumentException(String.format("Group type [%s] is not supported.",  group.getType()));
                }
            }
        }
    }

    private void exportGroups(CSVPrinter printer, Group group, String timezone, List<AreaGroupMemberEntity> areas) throws IOException {
        List<UUID> members = groupRepository.getGroupMemberKeys(group.getKey());

        for (UUID userKey : members) {
            List<Device> devices = deviceRepository.getUserDevices(userKey, EnumDeviceType.METER);
            if (devices.isEmpty()) {
                continue;
            }

            for (Device device : devices) {
                WaterMeterDevice meter = (WaterMeterDevice) device;

                printGroupRow(printer, group.getType().toString(), group.getKey(), meter.getSerial(), timezone);

                // Export group and area
                if (meter.getLocation() != null) {
                    for (AreaGroupMemberEntity area : areas) {
                        if (area.getGeometry().contains(meter.getLocation())) {
                            printGroupRow(printer, RECORD_TYPE_AREA, group.getKey(), meter.getSerial(), timezone, area.getKey());
                        }
                    }
                }
            }
        }
    }

    private void printGroupRow(CSVPrinter printer, String type, UUID groupKey, String serial, String timezone) throws IOException {
        this.printGroupRow(printer, type, groupKey, serial, timezone, null);
    }

    private void printGroupRow(CSVPrinter printer, String type, UUID groupKey, String serial, String timezone, UUID areaKey) throws IOException {
        List<String> row = new ArrayList<String>();

        row.add(type);
        row.add(groupKey.toString());
        if(areaKey == null) {
            row.add("");
        } else {
            row.add(areaKey.toString());
        }
        row.add(serial);
        row.add(timezone);

        printer.printRecord(row);
    }

    private void printUserRow(CSVPrinter printer, AuthenticatedUser user, WaterMeterDevice meter) throws IOException {
        List<String> row = new ArrayList<String>();

        row.add(meter.getSerial());
        row.add(user.getKey().toString());
        row.add(user.getUsername());
        row.add(user.getFirstname());
        row.add(user.getLastname());

        printer.printRecord(row);
    }

    @Override
    public void stop() {
        // TODO: Add business logic for stopping processing
    }

    /**
     * Enumeration of job input parameters.
     */
    public static enum EnumInParameter {
        /**
         * Utility id for which the data is exported.
         */
        UTILITY_ID("utility.id"),
        /**
         * Working directory
         */
        WORKING_DIRECTORY("working.directory"),
        /**
         * Group members export filename
         */
        OUTPUT_FILENAME_GROUPS("output.filename.groups"),
        /**
         * Users export filename
         */
        OUTPUT_FILENAME_USERS("output.filename.users");


        private final String value;

        public String getValue() {
            return value;
        }

        private EnumInParameter(String value) {
            this.value = value;
        }
    }
}
