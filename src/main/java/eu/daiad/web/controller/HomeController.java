package eu.daiad.web.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.joda.time.DateTimeZone;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import eu.daiad.web.security.model.DaiadUser;
import eu.daiad.web.security.model.EnumRole;


@Controller
public class HomeController {
	
    @RequestMapping("/")
    public String index(Model model, @AuthenticationPrincipal User activeUser) {
    	DaiadUser user = null;
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            user = ((DaiadUser) auth.getPrincipal());
        }

        if(user != null) {
        	model.addAttribute("firstname", user.getFirstname());
        	
            if(user.hasRole(EnumRole.ROLE_ADMIN)){
    			Set<String> zones = DateTimeZone.getAvailableIDs();
    			ArrayList<String> europe = new ArrayList<String>();
    			
				Iterator<String> it = zones.iterator();
			    while (it.hasNext()) {
			    	String zone = (String) it.next();
			    	if(zone.startsWith("Europe/")) {
			    		europe.add(zone);
			    	}
			    }
			    
            	model.addAttribute("timezones", europe.toArray());
            	model.addAttribute("userTimeZone", user.getTimezone());
            	
            	ArrayList<String[]> properties = new ArrayList<String[]>();
            	properties.add(new String[] {"Device name", "settings.device.name"});
            	properties.add(new String[] {"Calibrate", "settings.device.calibrate"});
            	properties.add(new String[] {"Unit", "settings.unit"});
            	properties.add(new String[] {"currency", "settings.currency"});
            	properties.add(new String[] {"Alarm", "settings.alarm"});
            	properties.add(new String[] {"Water cost", "settings.water.cost"});
            	properties.add(new String[] {"Cold water temperature", "settings.water.temperature-cold"});
            	properties.add(new String[] {"Heating system", "settings.energy.heating"});
            	properties.add(new String[] {"Efficiency", "settings.energy.efficiency"});
            	properties.add(new String[] {"Energy cost", "settings.energy.cost"});
            	properties.add(new String[] {"Share of solar", "settings.energy.solar"});
            	properties.add(new String[] {"Estimates showers per week", "settings.shower.estimate-per-week"});
            	properties.add(new String[] {"Time between showers", "settings.shower.time-between-shower"});
            	model.addAttribute("properties", properties);
            	
            	return "admin/index";
            }
            
            return "home/index";
        } 
        
        return "default";
    }
    
    
}
