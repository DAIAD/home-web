package eu.daiad.web.domain.application;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "tip")
@Table(schema = "public", name = "tip")
public class TipEntity
{
    @Id()
    @Column(name = "id")
    @SequenceGenerator(
        sequenceName = "tip_id_seq",
        name = "tip_id_seq",
        allocationSize = 1,
        initialValue = 1)
    @GeneratedValue(generator = "tip_id_seq", strategy = GenerationType.SEQUENCE)
    private int id;

    @Basic()
    @NotNull
    @Min(1)
    @NaturalId
    private int index;

    @Column(name = "locale", columnDefinition = "bpchar", length = 2)
    @NotNull
    @NaturalId
    private String locale;

    @ManyToOne()
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull
    private TipCategoryEntity category;

    @Basic()
    @NotNull
    private String title;

    @Basic()
    private String description;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "image_binary")
    @Type(type = "org.hibernate.type.BinaryType")
    private byte image[];

    @Column(name = "image_mime_type")
    private String imageMimeType;

    @Column(name = "image_link")
    private String imageLink;

    @Basic()
    private String prompt;

    @Column(name = "externa_link")
    private String externalLink;

    @Basic()
    private String source;

    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn;

    @Column(name = "modified_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime modifiedOn;

    @Column(name = "active")
    private boolean active = true;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public TipCategoryEntity getCategory() {
        return category;
    }

    public void setCategory(TipCategoryEntity category) {
        this.category = category;
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

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getExternalLink() {
        return externalLink;
    }

    public void setExternalLink(String externalLink) {
        this.externalLink = externalLink;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

    public DateTime getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(DateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getImageMimeType() {
        return imageMimeType;
    }

    public void setImageMimeType(String imageMimeType) {
        this.imageMimeType = imageMimeType;
    }

}
