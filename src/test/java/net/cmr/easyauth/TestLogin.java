package net.cmr.easyauth;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import net.cmr.easyauth.entity.EALogin;

@Entity
@Table(name= "test_users")
public class TestLogin extends EALogin {
    
}
