package eu.daiad.web.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.security.PasswordResetToken;
import eu.daiad.web.repository.application.IUserRepository;

/**
 * Provides the entry points to the DAIAD web application.
 */
@Controller
public class HomeController {

    /**
     * Google ReCAPTCHA site key.
     */
    @Value("${daiad.captcha.google.key}")
    private String googleReCAPTCHASiteKey;

    /**
     * Provides access to user data.
     */
    @Autowired
    private IUserRepository userRepository;

    /**
     * Renders the default web application page. If the user is already
     * authenticated, the client is redirected to the appropriate DAIAD
     * application depending on the user roles.
     *
     * @param user the authenticated user
     * @return the default DAIAD web page or the default page of the web
     *         application that is most appropriate for the user roles.
     */
    @RequestMapping("/")
    public String index(@AuthenticationPrincipal AuthenticatedUser user) {
        if (user != null) {
            if (user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
                return "redirect:/utility/";
            }

            return "redirect:/home/";
        }

        return "index";
    }

    /**
     * Renders the default HOME web application page.
     *
     * @param model the page mode.
     * @param user the authenticated user
     * @return the default HOME web page.
     */
    @RequestMapping("/home/**")
    public String home(Model model, @AuthenticationPrincipal AuthenticatedUser user) {
        if (user == null) {
            model.addAttribute("reload", false);
        } else {
            model.addAttribute("reload", true);
        }

        return "home/default";
    }

    /**
     * Renders the default UTILITY web application page.
     *
     * @param model the page mode.
     * @param user the authenticated user
     * @return the default UTILITY web page.
     */
    @RequestMapping("/utility/**")
    public String utility(Model model, @AuthenticationPrincipal AuthenticatedUser user) {
        if (user == null) {
            model.addAttribute("reload", false);
        } else {
            if (!user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
                return "redirect:/error/403";
            }
            model.addAttribute("reload", true);
        }

        return "utility/default";
    }

    /**
     * Resets the user password.
     *
     * @param model the page model.
     * @param user the authenticated user.
     * @param key the password reset token.
     * @return the name of the view to render.
     */
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
