{
	"locale": {
		"en":"English",
		"el":"Greek",
		"es":"Spanish",
		"de":"German"
	},
	"LoginForm": {
		"title": "Sign in to DAIAD@Utility",
		"password": {
			"reset": "Forgot password?"
		},
		"login": {
			"failure": "Authentication has failed. Please try again."
		},
		"button": {
			"signin": "Sign in",
			"signout": "Sign out"
		},
		"placehoder": {
			"username":"Email",
			"password":"Password"
		}
	},
	"Section": {
            "Dashboard": "Dashboard",
	    "Analytics":"Analytics",
	    "Forecasting": "Forecasting",
	    "Demographics": "Users & Groups",
	    "ModeManagement": "Mode Management",
	    "Search": "Search",
	    "Messages": "Messages",
	    "Settings": "Settings",
	    "Reporting": "Reporting",
	    "Alerts": "Alerts",
	    "Announcements" : "Announcements",
            "ManageAlerts" : "Manage Alerts",
	    "Scheduler": "Job Management",
	    "Consumers": "Consumers"
	},
	"Demographics" : {
		"Group" : "Groups"
	},
	"Settings" : {
		"User" : "User Preferences",
		"System" : "System Configuration"
	},
	"Table" : {
		"Group" : {
			"id": "Id",
			"name": "Name",
			"size": "# of Members",
			"createdOn": "Creation Date"
		},
		"User" : {
			"Users": "Users",
			"id": "Id",
			"active": "active",
			"name": "Name",
			"email": "E-mail",
			"group": "Group",
			"currentMode": "Current Mode",
			"viewInfoOnAmphiro": "b1",
			"viewInfoOnMobile": "Mobile",
			"allowSocial": "Social",
			"deactivateUser": "Deactivate User",
			"searchUsers": "Search users..."
		},
		"Alert": {
			"text" :"Description",
			"createdOn": "Creation Date",
			"acknowledged" : "Is Acknowledged"
		},
		"Save": "Save Changes"
	},
	"AddUserForm" : {
	  "PanelTitle" : "Add new user",
	  "MandatoryFields" : "Mandatory fields",
	  "ErrorsDetected" : "Errors were detected:",
	  "Success" : "Success!",
	  "FirstName" : {
	    "label" : "First Name",
	    "placeholder" : "Please enter First Name."
	  },
	  "LastName" : {
      "label" : "Last Name",
      "placeholder" : "Please enter Last Name."
    },
    "E-mail" : {
      "label" : "E-mail",
      "placeholder" : "Please enter E-mail."
    },
    "Gender" : {
      "label" : "Gender",
      "values" : {
        "Male" : "Male",
        "Female" : "Female"
      }
    },
    "Address" : {
      "label" : "Address",
      "placeholder" : "Please enter Address."
    },
    "Utility" : {
      "label" : "Utility"
    },
    "PostalCode" : {
      "label" : "Postal Code",
      "placeholder" : "Please enter Postal Code."
    }
	},
  "Buttons" : {
    "Cancel" : "Cancel",
    "Deactivate": "Deactivate",
    "SaveChanges": "Save Changes",
    "AddNewUser": "Add New User",
    "AddUser" : "Add User"
  },
	"Modal" : {
		"DeactivateUser" : {
			"Title": "User Deactivation",
			"Body" : {
				"Part1" : "Are you sure you wish to deactivate user \"",
				"Part2" : "\" (with id:\"",
				"Part3" : "\")?"
			}
		},
		"SaveChanges": {
			"Title": "Save Changes",
			"Body" : {
				"singular": " row has been modified. Are you sure you want to save this change?",
				"plural": " rows have been modified. Are you sure you want to save these changes?"
			}
		}
	},
	"FilterBar" : {
		"Filters": "Filters"
	},
	"Counter" : {
		"Users" : "Users",
		"Meters" : "Smart Meters",
		"Devices" : "Amphiro Devices"
	},
	"Error": {
		"400" : "Bad request",
		"403" : "Authentication has failed",
		"404" : "Not found",
		"500" : "Internal server error",
	  "ValidationError.NO_FIRST_NAME": "First name is missing.",
	  "ValidationError.NO_LAST_NAME": "Last name is missing.",
	  "ValidationError.NO_EMAIL": "E-mail address is missing.",
	  "ValidationError.NO_GENDER": "Gender is missing.",
	  "ValidationError.NO_ADDRESS": "Adress is missing.",
	  "ValidationError.NO_UTILITY": "Utility is missing.",
	  "ValidationError.NO_POSTAL_CODE": "Postal code is missing.",
	  "ValidationError.INVALID_EMAIL": "The E-mail address is invalid.",
    "ValidationError.TOO_LONG_FIRST_NAME" : "First name exceeds maximum length (40 characters).",
    "ValidationError.TOO_LONG_LAST_NAME" : "Last name exceeds maximum length (70 characters).",
    "ValidationError.TOO_LONG_EMAIL" : "E-mail exceeds maximum length (100 characters).",
    "ValidationError.TOO_LONG_ADDRESS" : "Address exceeds maximum length (90 characters).",
    "ValidationError.TOO_LONG_POSTAL_CODE" : "Postal code exceeds maximum length (10 characters).",
	  "UserErrorCode.USERNAME_EXISTS_IN_WHITELIST": "A user with this E-mail already exists in the user white list."
	}, 
	"Success": {
	  "UserSuccess.USER_ADDED_WHITELIST" : "User was succesfully registered in the user white list."
	}

}
