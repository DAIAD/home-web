package eu.daiad.web.security.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import eu.daiad.web.model.EnumGender;

public class DaiadUser extends User {

	private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

	private UUID key;
	
    private String firstname;
    
    private String lastname;
   
    private EnumGender gender;

	private DateTime birthdate;
    
    private String country;

    private String postalCode;

    private String timezone;
    
	public DaiadUser(UUID key, String username, String password) {
		super(username, password, new ArrayList<GrantedAuthority>());
		
		this.key = key;
	}
	
	public DaiadUser(UUID key, String username, String password, Collection<? extends GrantedAuthority> authorities) {
		super(username, password, authorities);
		
		this.key = key;
	}
	
    public String getFirstname() {
        return firstname;
    }
 
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
 
    public String getLastname() {
        return lastname;
    }
 
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    
    public UUID getKey() {
    	return this.key;
    }
        
	public EnumGender getGender() {
		return gender;
	}

	public void setGender(EnumGender gender) {
		this.gender = gender;
	}

	public DateTime getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(DateTime birthdate) {
		this.birthdate = birthdate;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
	
	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	
	public boolean hasRole(String role) {
		return this.getAuthorities().contains(new SimpleGrantedAuthority(role));
	}
	
	public boolean hasRole(EnumRole role) {
		return this.getAuthorities().contains(new SimpleGrantedAuthority(role.toString()));
	}

	@Override
	public String toString() {
		Object[] authorities = this.getAuthorities().toArray();
		ArrayList<String> roles = new ArrayList<String>();
		for(int index=0, count = authorities.length; index < count; index++) {
			roles.add(((GrantedAuthority) authorities[index]).getAuthority());
		}
		return "DaiadUser [key=" + key + ", username=" + this.getUsername() + ", firstname=" + firstname
				+ ", lastname=" + lastname + ", gender=" + gender
				+ ", birthdate=" + birthdate + ", country=" + country
				+ ", postalCode=" + postalCode + ", roles=" + StringUtils.join(roles, ",") +  "]";
	}
	
	
}