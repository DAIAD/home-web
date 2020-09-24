package eu.daiad.scheduler.service.etl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.daiad.common.domain.admin.ExportFileEntity;
import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.export.EnumDataExportType;
import eu.daiad.common.repository.application.IExportRepository;
import eu.daiad.common.service.BaseService;

/**
 * Provides methods for exporting data from HBASE and PostgreSQL
 */
@Service
public class DefaultDataExportService extends BaseService implements IDataExportService {

    /**
     * Folder where temporary files are saved.
     */
    @Value("${tmp.folder}")
    private String workingDirectory;

    /**
     * Service for exporting user data.
     */
    @Autowired
    private UserDataExportService userDataExportService;

    /**
     * Service for exporting utility meter data.
     */
    @Autowired
    private UtilityMeterDataExportService  utilityMeterDataExportService;

    /**
     * Service for exporting utility amphiro b1 data.
     */
    @Autowired
    private UtilityAmphiroDataExportService  utilityAmphiroDataExportService;

    /**
     * Repository for accessing exported data files.
     */
    @Autowired
    private IExportRepository exportRepository;

    /**
     * Exports data for a single user to a temporary file.
     *
     * @param query the query that selects the data to export.
     * @return a unique token for downloading the exported file.
     *
     * @throws ApplicationException if the query execution or file creation fails.
     */
    @Override
    public String export(UserDataExportQuery query) throws ApplicationException {
        try {
            String token = UUID.randomUUID().toString();

            // Export data
            ExportResult result = userDataExportService.export(query);

            // Create archive
            File zipFile = new File(FilenameUtils.concat(workingDirectory, token + ".zip"));

            compressFiles(result.getFiles(), zipFile);

            // Cleanup
            for (FileLabelPair file : result.getFiles()) {
                FileUtils.deleteQuietly(file.getFile());
            }

            return token;
        } catch (Exception ex) {
            throw wrapApplicationException(ex);
        }
    }

    /**
     * Exports data for a single utility to a file. Any exported data file is replaced.
     *
     * @param query the query that selects the data to export.
     *
     * @throws ApplicationException if the query execution or file creation fails.
     */
    @Override
    public void export(UtilityDataExportQuery query) throws ApplicationException {
        ExportResult result = null;
        if(query.isExportFinalTrialData()) {
            query.setStartTimestamp((new DateTime(2016, 3, 1, 0, 0, DateTimeZone.forID(query.getTimezone()))).getMillis());
            query.setEndTimestamp((new DateTime(2017, 3, 1, 0, 0, DateTimeZone.forID(query.getTimezone()))).getMillis());
        }
        try {
            switch (query.getSource()) {
                case AMPHIRO:
                    result = utilityAmphiroDataExportService.export(query);
                    break;
                case METER:
                    result = utilityMeterDataExportService.export(query);
                    break;
                default:
                    // Do nothing
                    break;
            }

            // Compress files and update export record
            if (!result.isEmpty()) {
                // Optionally rename or compress file(s)
                if ((query.isComporessed()) || (result.getFiles().size() > 1)) {
                    // Always compress files if output consists of more than one
                    // files.
                    String zipFilename = buildZipFilename(query.getTargetDirectory(), query.getFilename(), "zip", !query.isExportFinalTrialData());
                    File zipFile = new File(zipFilename);
                    if(zipFile.exists()) {
                        FileUtils.deleteQuietly(zipFile);
                    }

                    compressFiles(result.getFiles(), zipFile);

                    // Delete all unused files
                    for (FileLabelPair file : result.getFiles()) {
                        if (file.getFile().exists()) {
                            FileUtils.deleteQuietly(file.getFile());
                        }
                    }

                    // Log export
                    saveExport(query, result, zipFile);
                } else {
                    // Rename/Move output
                    FileUtils.moveFile(result.getFiles().get(0).getFile(),
                                       new File(FilenameUtils.concat(query.getTargetDirectory(), query.getFilename())));
                }
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex);
        }
    }

    /**
     * Log exported file to database.
     *
     * @param query data export query.
     * @param result the export result.
     * @param zipFile the new archive.
     */
    private void saveExport(UtilityDataExportQuery query, ExportResult result, File zipFile) {
        // Create new export record
        ExportFileEntity export = new ExportFileEntity();

        export.setFilename(FilenameUtils.getName(zipFile.getPath()));
        export.setPath(FilenameUtils.getFullPath(zipFile.getPath()));
        export.setCreatedOn(new DateTime());
        export.setStartedOn(export.getCreatedOn());
        export.setUtilityId(query.getUtility().getId());
        export.setUtilityName(query.getUtility().getName());
        export.setDescription(query.getDescription());

        export.setSize(zipFile.length());
        export.setCompletedOn(new DateTime());
        export.setTotalRows(result.getTotalRows());
        export.setHidden(false);
        export.setPinned(query.isExportFinalTrialData());
        export.setType(query.isExportFinalTrialData() ? EnumDataExportType.DATA_EXPORT_TRIAL : EnumDataExportType.DATA_EXPORT);

        if(query.isExportFinalTrialData()) {
            exportRepository.replace(export);
        } else {
            exportRepository.create(export);
        }
    }

    /**
     * Creates a filename for the utility exported data file.
     *
     * @param targetDirectory target directory
     * @param filename filename prefix.
     * @param extension filename extension.
     * @param appendDateTime when true, the date and time is appended to the file name.
     * @return the filename.
     */
    private String buildZipFilename(String targetDirectory, String filename, String extension, boolean appendDateTime) {
        if(StringUtils.isBlank(filename)) {
            filename = "export";
        } else {
            filename = filename.replaceAll("[^a-zA-Z0-9]", "-");
        }
        if(appendDateTime) {
            DateTimeFormatter fileDateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd-HHmmss");

            if(StringUtils.isBlank(extension)) {
                filename = String.format("%s-%s", filename.toLowerCase(), new DateTime().toString(fileDateFormatter));
            } else {
                filename = String.format("%s-%s.%s", filename.toLowerCase(), new DateTime().toString(fileDateFormatter), extension);
            }
        } else {
            if(StringUtils.isBlank(extension)) {
                filename = String.format("%s", filename.toLowerCase());
            } else {
                filename = String.format("%s.%s", filename.toLowerCase(), extension);
            }
        }

        return FilenameUtils.concat(targetDirectory, filename);
    }


    /**
     * Compresses one or more files to a single archive of ZIP format.
     *
     * @param files the files to add to the archive.
     * @param output the final archive.
     * @throws IOException in case any I/O exception occurs.
     */
    private void compressFiles(List<FileLabelPair> files, File output) throws IOException {
        compressFiles(files.toArray(new FileLabelPair[] {}), output);
    }

    /**
     * Compresses one or more files to a single archive of ZIP format.
     *
     * @param files the files to add to the archive.
     * @param output the final archive.
     * @throws IOException in case any I/O exception occurs.
     */
    private void compressFiles(FileLabelPair[] files, File output) throws IOException {
        byte[] buffer = new byte[4096];
        int length;

        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(output));

        for (FileLabelPair file : files) {
            if(file.getFile().exists()) {
                ZipEntry zipEntry = new ZipEntry(file.getLabel());
                zipOutputStream.putNextEntry(zipEntry);

                FileInputStream in = new FileInputStream(file.getFile());

                while ((length = in.read(buffer)) > 0) {
                    zipOutputStream.write(buffer, 0, length);
                }
                in.close();

                zipOutputStream.flush();
                zipOutputStream.closeEntry();
            }
        }

        zipOutputStream.close();
    }
}
