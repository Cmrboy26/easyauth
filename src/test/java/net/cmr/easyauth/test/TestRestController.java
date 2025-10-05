package net.cmr.easyauth.test;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RestController;

import net.cmr.easyauth.controller.EARestController;

@RestController
@Profile("test")
public class TestRestController extends EARestController<TestLogin> {
    
}
