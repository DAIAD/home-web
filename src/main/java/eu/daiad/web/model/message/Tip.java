package eu.daiad.web.model.message;

import java.nio.charset.Charset;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.security.crypto.codec.Base64;

public class Tip extends Message
{    
    private int index = -1;

    private String description;

    private String categoryName;

    @JsonIgnore
    private byte[] image;

    private String imageMimeType;

    private String imageLink;

    private String prompt;

    private String externalLink;

    private String source;

    private Long modifiedOn;

    private boolean active;

    public Tip()
    {
        super();
    }

    public Tip(int id)
    {
        super(id);
    }

    @JsonIgnore
    @Override
    public EnumMessageType getType() 
    {
        return EnumMessageType.TIP;
    }
    
    // Todo: replace with getType (only for API compatibility reasons) 
    @JsonProperty("type")
    public String getTypeAsLegacyName() 
    {
        return "RECOMMENDATION_STATIC";
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    @JsonProperty("imageEncoded")
    public byte[] getImage() 
    {
        return image;
    }

    @JsonProperty("imageEncoded")
    public void setImage(byte[] image) 
    {
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
    
    @JsonProperty("modifiedOn")
    public Long getModifiedOn() {
        return modifiedOn;
    }

    @JsonProperty("modifiedOn")
    public void setModifiedOn(long modified)
    {
        this.modifiedOn = modified;
    }

    @JsonIgnore
    public void setModifiedOn(DateTime modified)
    {
        this.modifiedOn = modified.getMillis();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getExternalLink() {
        return externalLink;
    }

    public void setExternalLink(String externalLink) {
        this.externalLink = externalLink;
    }

    public String getImageMimeType() {
        return imageMimeType;
    }

    public void setImageMimeType(String imageMimeType) {
        this.imageMimeType = imageMimeType;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    @Override
    public String getBody()
    {
        return description;
    }

//    @Override
//    public Object[] toRowData()
//    {
//        return new Object[] {
//           Integer.valueOf(getId()),
//           getType(),
//           categoryName,
//           title,
//           description,
//           createdOn,
//           acknowledgedOn
//        };
//    }
//
//    @Override
//    public String[] toRowHeaders()
//    {
//        return new String[] {
//            "Id",
//            "Type",
//            "Category-Name",
//            "Title",
//            "Description",
//            "Created",
//            "Acknowledged"
//        };
//    }
}
