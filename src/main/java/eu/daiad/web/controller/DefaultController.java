package eu.daiad.web.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;


@Controller
public class DefaultController {

	@Value("${custom.security.user.name}")
	private String username;
	
	@RequestMapping("/")
	public String index(Model model) {
		model.addAttribute("username", username);
		return "index";
	}

}
