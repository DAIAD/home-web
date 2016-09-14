package eu.daiad.web.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.security.PasswordResetToken;
import eu.daiad.web.repository.application.IUserRepository;

@Controller
public class HomeController {

    @Value("${daiad.captcha.google.key}")
    private String googleReCAPTCHASiteKey;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private IUserRepository userRepository;
    
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

    @RequestMapping("/password/reset/{key}")
    public String passwordReset(Model model, @AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID key) {
        if (key == null) {
            return "redirect:/error/404";
        }

        PasswordResetToken token = userRepository.getPasswordResetTokenById(key);
        if (token == null) {
            return "redirect:/error/404";
        }
        if (user != null) {
            // Authenticated users are not allowed to reset their password
            return "redirect:/error/403";
        }

        model.addAttribute("reload", false);
        model.addAttribute("googleReCAPTCHASiteKey", googleReCAPTCHASiteKey);
        model.addAttribute("locale", token.getLocale());

        return "utility/default";
    }

}
