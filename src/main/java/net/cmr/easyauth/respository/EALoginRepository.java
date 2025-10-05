package net.cmr.easyauth.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import net.cmr.easyauth.entity.EALogin;

@NoRepositoryBean
public abstract interface EALoginRepository<L extends EALogin> extends JpaRepository<L, Long> {
    
}
