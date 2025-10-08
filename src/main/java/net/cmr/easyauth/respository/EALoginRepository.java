package net.cmr.easyauth.respository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import net.cmr.easyauth.entity.EALogin;

@NoRepositoryBean
public abstract interface EALoginRepository<L extends EALogin> extends JpaRepository<L, Long> {
    Optional<L> findByUsername(String username); 
    @Query("SELECT l FROM #{#entityName} l LEFT JOIN FETCH l.authorities WHERE l.id = :id")
    Optional<L> findByIdWithAuthorities(@Param("id") Long id);
}
