package eu.daiad.web.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.daiad.web.security.model.ApplicationUser;
import eu.daiad.web.security.model.EnumRole;

@Controller
public class HomeController {

	@RequestMapping("/")
	public String index(Model model, @AuthenticationPrincipal User user) {
		ApplicationUser daiadUser = (ApplicationUser) user;

		if (daiadUser != null) {
			if (daiadUser.hasRole(EnumRole.ROLE_ADMIN)) {
				return "redirect:/utility/";
			}

			return "redirect:/home/";
		}

		return "index";
	}

	@RequestMapping("/home/")
	public String home(Model model, @AuthenticationPrincipal User user) {
		ApplicationUser daiadUser = (ApplicationUser) user;

		if (daiadUser == null) {
			model.addAttribute("reload", false);
		} else {
			model.addAttribute("reload", true);
		}

		return "home/default";
	}

	@RequestMapping("/utility/")
	public String utility(Model model, @AuthenticationPrincipal User user) {
		ApplicationUser daiadUser = (ApplicationUser) user;

		if (daiadUser == null) {
			model.addAttribute("reload", false);
		} else {
			model.addAttribute("reload", true);

			if (!daiadUser.hasRole(EnumRole.ROLE_ADMIN)) {
				return "redirect:/error/403";
			}
		}

		return "utility/default";
	}

}
