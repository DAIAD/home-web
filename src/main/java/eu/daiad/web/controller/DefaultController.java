package eu.daiad.web.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import eu.daiad.web.security.model.DaiadUser;


@Controller
public class DefaultController {
	
	@RequestMapping("/")
	public String index(Model model, @AuthenticationPrincipal User activeUser) {
		String username = "";
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (!(auth instanceof AnonymousAuthenticationToken)) {
			username = ((DaiadUser) auth.getPrincipal()).getFirstname();
		}
		
		model.addAttribute("username", username);
		return "index";
	}

}
