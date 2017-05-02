package eu.daiad.web.service.mail;

public class EmailAddress {

    private String name;

    private String address;

    public EmailAddress(String address) {
        this.address = address;
    }

    public EmailAddress(String address, String name) {
        this.address = address;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "EmailAddress [name=" + name + ", address=" + address + "]";
    }

}
