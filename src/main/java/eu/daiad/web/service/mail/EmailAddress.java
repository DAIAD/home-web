package eu.daiad.web.service.mail;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class EmailAddress {

    @Getter
    @Setter
    private String name;

    @NotEmpty
    @Email
    @Getter
    @Setter
    private String address;

    public EmailAddress(String address) {
        this.address = address;
    }

    public EmailAddress(String address, String name) {
        this.address = address;
        this.name    = name;
    }

    @Override
    public String toString() {
        return "EmailAddress [name=" + this.name + ", address=" + this.address + "]";
    }

}