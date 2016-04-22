package eu.daiad.web.domain.application;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author nkarag
 */
@Entity(name = "alert_translation")
@Table(schema = "public", name = "alert_translation")
public class AlertTranslation {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "alert_translation_id_seq", name = "alert_translation_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "alert_translation_id_seq", strategy = GenerationType.SEQUENCE)
    private int id;

    @Column(name = "alert_id", nullable = false)
    private int alertId;

    @Column(name = "locale", columnDefinition = "bpchar", length = 2)
    private String locale;

    @Basic()
    private String title;

    @Basic()
    private String description;

    @Column(name = "link")
    private String imageLink;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAlertId() {
        return alertId;
    }

    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

}
