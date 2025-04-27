package com.barmao.task.manager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Simple controller that redirects to the H2 console
 */
@Controller
public class H2ConsoleController {

    /**
     * Redirect /db to the H2 console for easier access
     */
    @GetMapping("/db")
    public RedirectView redirectToH2Console() {
        return new RedirectView("/h2-console");
    }
}