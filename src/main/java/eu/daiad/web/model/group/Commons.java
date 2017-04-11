package eu.daiad.web.model.group;

public class Commons extends Group {

    private String description;

    private byte[] image;

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

    @Override
    public EnumGroupType getType() {
        return EnumGroupType.COMMONS;
    }

}
