package net.cmr.easyauth.test;

import org.springframework.context.annotation.Profile;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import net.cmr.easyauth.entity.EALogin;

@Entity
@Table(name= "test_users")
@Profile("test")
public class TestLogin extends EALogin {

    public TestLogin() { }

    public TestLogin(String username, String password) {
        super(username, password);
    }
    
}
