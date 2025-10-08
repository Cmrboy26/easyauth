package net.cmr.easyauth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebpageController {

    private static boolean enableLoginWebpage;

    @Value("{cmr.easyauth.enableLoginWebpage:true}")
    private void setPrioritizeHeaders(String enableLoginWebpage) {
        WebpageController.enableLoginWebpage = Boolean.valueOf(enableLoginWebpage);
    }

    @GetMapping("/auth/login")
    public String login(Model model) {
        if (enableLoginWebpage) {
            return "login";
        }
        // Redirect to home page or show an error page if login page is disabled
        return "redirect:/";
    }
}
