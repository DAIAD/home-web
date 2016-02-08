package eu.daiad.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;

@Controller
public class HomeController {

	@Autowired
	private MessageSource messageSource;

	@RequestMapping("/")
	public String index(Model model, @AuthenticationPrincipal AuthenticatedUser user) {
		if (user != null) {
			if (user.hasRole(EnumRole.ROLE_ADMIN)) {
				return "redirect:/utility/";
			}

			return "redirect:/home/";
		}

		return "index";
	}

	@RequestMapping("/home/**")
	public String home(Model model, @AuthenticationPrincipal AuthenticatedUser user) {
		if (user == null) {
			model.addAttribute("reload", false);
		} else {
			model.addAttribute("reload", true);
		}

		return "home/default";
	}

	@RequestMapping("/utility/**")
	public String utility(Model model, @AuthenticationPrincipal AuthenticatedUser user) {
		if (user == null) {
			model.addAttribute("reload", false);
		} else {
			if (!user.hasRole(EnumRole.ROLE_ADMIN)) {
				return "redirect:/error/403";
			}
			model.addAttribute("reload", true);
		}

		return "utility/default";
	}

}
