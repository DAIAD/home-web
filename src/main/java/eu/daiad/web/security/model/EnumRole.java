package eu.daiad.web.security.model;

public enum EnumRole {
	ROLE_NONE,
	ROLE_USER,
	ROLE_ADMIN;
		
	public static EnumRole fromString(String value) {
		 for (EnumRole item : EnumRole.values()) {
	        if (item.name().equalsIgnoreCase(value)) {
            	return item;
	        }
	    }
        return EnumRole.ROLE_NONE;
	}

}
