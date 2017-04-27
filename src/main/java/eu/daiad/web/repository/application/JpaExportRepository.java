package eu.daiad.web.repository.application;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.admin.ExportFileEntity;
import eu.daiad.web.model.export.DataExportFileQuery;
import eu.daiad.web.model.export.DataExportFileQueryResult;
import eu.daiad.web.model.export.ExportFile;

/**
 * Provides methods for managing exported data files
 */
@Repository
@Transactional("managementTransactionManager")
public class JpaExportRepository implements IExportRepository {

    /**
     * Entity manager for persisting export meta data.
     */
    @PersistenceContext(unitName = "management")
    EntityManager entityManager;

    /**
     * Store an instance of {@link ExportFileEntity}.
     *
     * @param entity the entity to store.
     */
    @Override
    public void create(ExportFileEntity entity) {
        entityManager.persist(entity);
        entityManager.flush();
    }

    /**
     * Store an instance of {@link ExportFileEntity}, replacing any export with the same filename.
     *
     * @param entity the entity to store.
     */
    @Override
    public void replace(ExportFileEntity entity) {
        String queryString = "select e from export e where e.filename = :filename and e.utilityId = :utilityId";

        List<ExportFileEntity> existing = entityManager.createQuery(queryString, ExportFileEntity.class)
                                                       .setParameter("filename", entity.getFilename())
                                                       .setParameter("utilityId", entity.getUtilityId())
                                                       .setMaxResults(1)
                                                       .getResultList();
        if(existing.size() > 0) {
            entityManager.remove(existing.get(0));
        }

        create(entity);
    }

    /**
     * Get an exported file with the given key.
     *
     * @param key of the file.
     * @return the exported file.
     */
    @Override
    public ExportFile getExportFileByKey(UUID key) {
        String queryString = "select e from export e where e.key = :key";

        List<ExportFileEntity> entities = entityManager.createQuery(queryString, ExportFileEntity.class)
                                                       .setParameter("key", key)
                                                       .setMaxResults(1)
                                                       .getResultList();

        return (entities.isEmpty() ? null : exportEntityToObject(entities.get(0)));
    }

    /**
     * Get all exported files for a specific utility.
     *
     * @param query query that selects exported data files.
     * @return a list of the exported data files.
     */
    @Override
    public DataExportFileQueryResult getAllExportFiles(DataExportFileQuery query) {
        int total;
        List<ExportFile> files;

        // Count
        String qlStringCount = "select count(e.id) from export e where e.utilityId in :utilities and e.type = :type";

        TypedQuery<Number> countQuery = entityManager.createQuery(qlStringCount, Number.class)
                                                     .setParameter("utilities", query.getUtilities())
                                                     .setParameter("type",query.getType());

        total = ((Number) countQuery.getSingleResult()).intValue();

        // Select
        String qlStringSelect = "select e from export e where e.utilityId in :utilities and e.type = :type order by e.createdOn desc, e.filename";

        TypedQuery<ExportFileEntity> selectQuery = entityManager.createQuery(qlStringSelect, ExportFileEntity.class)
                                                                .setParameter("utilityId", query.getUtilities())
                                                                .setParameter("type",query.getType());

        if ((query.getIndex() != null) && (query.getSize() != null)) {
            selectQuery.setFirstResult(query.getIndex() * query.getSize());
        } else {
            selectQuery.setFirstResult(0);
        }
        if (query.getSize() != null) {
            selectQuery.setMaxResults(query.getSize());
        }

        files = exportEntityListToObjectList(selectQuery.getResultList());

        // Compose response
        return new DataExportFileQueryResult(total, files);

    }

    /**
     * Get all valid exported files for a specific utility.
     *
     * @param query query that selects exported data files.
     * @return a list of valid exported data files.
     */
    @Override
    public DataExportFileQueryResult getNotExpiredExportFiles(DataExportFileQuery query) {
        int total;
        List<ExportFile> files;

        // Count
        String qlStringCount = "select count(e.id) " +
                               "from   export e " +
                               "where   e.utilityId in :utilities and " +
                               "        (e.createdOn >= :createdOn or e.pinned = true) and " +
                               "        e.type = :type";

        TypedQuery<Number> countQuery = entityManager.createQuery(qlStringCount, Number.class)
                                                     .setParameter("utilities", query.getUtilities())
                                                     .setParameter("createdOn", new DateTime().minusDays(query.getDays()))
                                                     .setParameter("type",query.getType());

        total = ((Number) countQuery.getSingleResult()).intValue();

        // Select
        String qlStringSelect = "select e from export e " +
                                "where  e.utilityId in :utilities and " +
                                "       (e.createdOn >= :createdOn or e.pinned = true) and " +
                                "       e.type = :type " +
                                "order by e.createdOn desc, e.filename";

        TypedQuery<ExportFileEntity> selectQuery = entityManager.createQuery(qlStringSelect, ExportFileEntity.class)
                                                                .setParameter("utilities", query.getUtilities())
                                                                .setParameter("createdOn", new DateTime().minusDays(query.getDays()))
                                                                .setParameter("type",query.getType());

        if ((query.getIndex() != null) && (query.getSize() != null)) {
            selectQuery.setFirstResult(query.getIndex() * query.getSize());
        } else {
            selectQuery.setFirstResult(0);
        }
        if (query.getSize() != null) {
            selectQuery.setMaxResults(query.getSize());
        }

        files = exportEntityListToObjectList(selectQuery.getResultList());

        // Compose response
        return new DataExportFileQueryResult(total, files);
    }

    /**
     * Get all expired exported files for a specific utility.
     *
     * @param query query that selects exported data files.
     * @return a list of expired exported data files.
     */
    @Override
    public DataExportFileQueryResult getExpiredExportFiles(DataExportFileQuery query) {
        int total;
        List<ExportFile> files;

        // Count
        String qlStringCount = "select count(e.id) " +
                               "from    export e " +
                               "where   e.utilityId in :utilities and " +
                               "        (e.createdOn < :createdOn and pinned = false) and " +
                               "        e.type = :type";

        TypedQuery<Number> countQuery = entityManager.createQuery(qlStringCount, Number.class)
                                                     .setParameter("utilities", query.getUtilities())
                                                     .setParameter("createdOn", new DateTime().minusDays(query.getDays()))
                                                     .setParameter("type", query.getType());

        total = ((Number) countQuery.getSingleResult()).intValue();

        // Select
        String qlStringSelect = "select e from export e " +
                                "where  e.utilityId in :utilities and " +
                                "       (e.createdOn < :createdOn and pinned = false) and " +
                                "       e.type = :type " +
                                "order by e.createdOn desc, e.filename";

        TypedQuery<ExportFileEntity> selectQuery = entityManager.createQuery(qlStringSelect, ExportFileEntity.class);
        selectQuery.setParameter("utilities", query.getUtilities());
        selectQuery.setParameter("createdOn", new DateTime().minusDays(query.getDays()));

        if ((query.getIndex() != null) && (query.getSize() != null)) {
            selectQuery.setFirstResult(query.getIndex() * query.getSize());
        } else {
            selectQuery.setFirstResult(0);
        }
        if (query.getSize() != null) {
            selectQuery.setMaxResults(query.getSize());
        }

        files = exportEntityListToObjectList(selectQuery.getResultList());

        // Compose response
        return new DataExportFileQueryResult(total, files);
    }

    /**
     * Deletes all expired exported files for a specific utility.
     *
     * @param utilityId the utility id.
     * @param days the number of days after which a file is marked as expired.
     */
    @Override
    public void deleteExpiredExportFiles(int utilityId, int days) {
        String queryString = "select e from export e where e.utilityId = :utilityId and e.createdOn < :createdOn and e.pinned = false";

        TypedQuery<ExportFileEntity> query = entityManager.createQuery(queryString, ExportFileEntity.class)
                                                          .setParameter("utilityId", utilityId)
                                                          .setParameter("createdOn", new DateTime().minusDays(days));


        List<ExportFileEntity> entities = query.getResultList();

        for(ExportFileEntity entity : entities) {
            entityManager.remove(entity);

            File file = new File(FilenameUtils.concat(entity.getPath(), entity.getFilename()));

            if(file.exists()) {
                FileUtils.deleteQuietly(file);
            }
        }
    }

    /**
     * Creates a list of @{link ExportFile} from a list of @{link ExportFileEntity}
     *
     * @param entities the list of @{link ExportFileEntity} to convert.
     * @return a new list of @{link ExportFile}.
     */
    private List<ExportFile> exportEntityListToObjectList(List<ExportFileEntity> entities) {
        List<ExportFile> objects = new ArrayList<ExportFile>();

        for(ExportFileEntity entity : entities) {
            objects.add(exportEntityToObject(entity));
        }

        return objects;
    }

    /**
     * Creates an instance of @{link ExportFile} from an instance of @{link ExportFileEntity}
     *
     * @param entity the entity to convert.
     * @return a new instance of @{link ExportFile}.
     */
    private ExportFile exportEntityToObject(ExportFileEntity entity) {
        ExportFile obj = new ExportFile();

        obj.setCompletedOn(entity.getCompletedOn());
        obj.setCreatedOn(entity.getCompletedOn());
        obj.setDescription(entity.getDescription());
        obj.setFilename(entity.getFilename());
        obj.setKey(entity.getKey());
        obj.setPath(entity.getPath());
        obj.setSize(entity.getSize());
        obj.setStartedOn(entity.getStartedOn());
        obj.setTotalRows(entity.getTotalRows());
        obj.setUtilityId(entity.getUtilityId());
        obj.setUtilityName(entity.getUtilityName());
        obj.setType(entity.getType());

        return obj;
    }
}
