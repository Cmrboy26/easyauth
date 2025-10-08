package net.cmr.easyauth.test;

import org.springframework.context.annotation.Profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import net.cmr.easyauth.entity.EALogin;
import net.cmr.easyauth.util.NonNullMap;
import net.cmr.easyauth.util.NonNullMap.NullValueException;

@Entity
@Table(name= "test_users")
@Profile("test")
public class TestLogin extends EALogin {

    @Column(name = "email", nullable = true, unique = true)
    private String email;
    @Column(name = "name", nullable = false)
    private String name;

    public TestLogin() { }

    public TestLogin(NonNullMap<String, String> registerParams) {
        super(registerParams);
        this.email = registerParams.getNullable("email");
        this.name = registerParams.get("name");
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
}
