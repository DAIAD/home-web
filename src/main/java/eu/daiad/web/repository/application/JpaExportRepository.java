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
        this.entityManager.persist(entity);
        this.entityManager.flush();
    }

    /**
     * Get an exported file with the given id.
     * 
     * @param id of the file.
     * @return the exported file.
     */
    @Override
    public ExportFile getExportFileById(int id) {
        String queryString = "select e from export e where e.id = :id";

        TypedQuery<ExportFileEntity> query = entityManager.createQuery(queryString, ExportFileEntity.class)
                                                          .setMaxResults(1);

        query.setParameter("id", id);

        List<ExportFileEntity> entities = query.getResultList();

        if(entities.isEmpty()) {
            return null;
        }
        
        return exportEntityToObject(entities.get(0));
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

        TypedQuery<ExportFileEntity> query = entityManager.createQuery(queryString, ExportFileEntity.class)
                                                          .setMaxResults(1);

        query.setParameter("key", key);

        List<ExportFileEntity> entities = query.getResultList();

        if(entities.isEmpty()) {
            return null;
        }
        
        return exportEntityToObject(entities.get(0));  
    }
    
    /**
     * Get all exported files for a specific utility.
     * 
     * @param utilityId the utility id.
     * @return a list of the exported data files.
     */
    @Override
    public List<ExportFile> getAllExportFilesByUtilityId(int utilityId) {
        String queryString = "select e from export e where e.utilityId = :utilityId";

        TypedQuery<ExportFileEntity> query = entityManager.createQuery(queryString, ExportFileEntity.class);

        query.setParameter("utilityId", utilityId);

        return exportEntityListToObjectList(query.getResultList());
    }
    
    /**
     * Get all valid exported files for a specific utility.
     * 
     * @param utilityId the utility id.
     * @param days the number of days after which a file is marked as expired.
     * @return a list of valid exported data files.
     */
    @Override
    public List<ExportFile> getValidExportFilesByUtilityId(int utilityId, int days) {
        String queryString = "select e from export e where e.utilityId = :utilityId and e.createdOn >= :createdOn";

        TypedQuery<ExportFileEntity> query = entityManager.createQuery(queryString, ExportFileEntity.class);

        query.setParameter("utilityId", utilityId);
        query.setParameter("createdOn", new DateTime().minusDays(days));

        return exportEntityListToObjectList( query.getResultList());
    }

    /**
     * Get all expired exported files for a specific utility.
     * 
     * @param utilityId the utility id.
     * @param days the number of days after which a file is marked as expired.
     * @return a list of expired exported data files.
     */
    @Override
    public List<ExportFile> getExpiredExportFilesByUtilityId(int utilityId, int days) {
        String queryString = "select e from export e where e.utilityId = :utilityId and e.createdOn < :createdOn";

        TypedQuery<ExportFileEntity> query = entityManager.createQuery(queryString, ExportFileEntity.class);

        query.setParameter("utilityId", utilityId);
        query.setParameter("createdOn", new DateTime().minusDays(days));

        return exportEntityListToObjectList(query.getResultList());        
    }
    
    /**
     * Deletes all expired exported files for a specific utility.
     * 
     * @param utilityId the utility id.
     * @param days the number of days after which a file is marked as expired.
     */
    @Override
    public void deleteExpiredExportFiles(int utilityId, int days) {
        String queryString = "select e from export e where e.utilityId = :utilityId and e.createdOn < :createdOn";

        TypedQuery<ExportFileEntity> query = entityManager.createQuery(queryString, ExportFileEntity.class);

        query.setParameter("utilityId", utilityId);
        query.setParameter("createdOn", new DateTime().minusDays(days));

    
        List<ExportFileEntity> entities = query.getResultList();
        
        for(ExportFileEntity entity : entities) {
            this.entityManager.remove(entity);
            
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

        return obj;
    }
}
