package eu.daiad.web.service.etl;

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
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.daiad.web.domain.admin.ExportFileEntity;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.repository.application.IExportRepository;
import eu.daiad.web.service.BaseService;

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
            File zipFile = new File(FilenameUtils.concat(this.workingDirectory, token + ".zip"));

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

        try {
            String zipFilename = buildFilename(query.getTargetDirectory(), query.getFilename(), "zip");
            File zipFile = new File(zipFilename);

            // Create new export record
            ExportFileEntity export = new ExportFileEntity();

            export.setFilename(FilenameUtils.getName(zipFilename));
            export.setPath(FilenameUtils.getFullPath(zipFilename));
            export.setCreatedOn(new DateTime());
            export.setStartedOn(export.getCreatedOn());
            export.setUtilityId(query.getUtility().getId());
            export.setUtilityName(query.getUtility().getName());
            export.setDescription(query.getDescription());

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

            // Compress
            if (!result.getFiles().isEmpty()) {
                compressFiles(result.getFiles(), zipFile);

                // Delete all unused files
                for(FileLabelPair file : result.getFiles()) {
                    FileUtils.deleteQuietly(file.getFile());
                }
            }

            // Update export record
            if (result.getTotalRows() != 0) {
                export.setSize(zipFile.length());
                export.setCompletedOn(new DateTime());
                export.setTotalRows(result.getTotalRows());

                exportRepository.create(export);
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex);
        }
    }

    /**
     * Creates a filename for the utility exported data file.
     *
     * @param targetDirectory target directory
     * @param filename filename prefix.
     * @param extension filename extension.
     * @return the filename.
     */
    private String buildFilename(String targetDirectory, String filename, String extension) {
        if(StringUtils.isBlank(filename)) {
            filename = "export";
        } else {
            filename = filename.replaceAll("[^a-zA-Z0-9]", "-");
        }

        DateTimeFormatter fileDateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");

        if(StringUtils.isBlank(extension)) {
            filename = String.format("%s-%s", filename.toLowerCase(), new DateTime().toString(fileDateFormatter));
        } else {
            filename = String.format("%s-%s.%s", filename.toLowerCase(), new DateTime().toString(fileDateFormatter), extension);
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

        zipOutputStream.close();
    }
}
