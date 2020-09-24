package eu.daiad.scheduler.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Provides the entry points to the DAIAD web application.
 */
@Controller
public class HomeController extends BaseController{

    @RequestMapping("/")
    public String index() {
        return "index";
    }

}
