package net.cmr.easyauth.respository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.cmr.easyauth.entity.EAAuthority;

@Repository
public abstract interface EAAuthorityRepository extends JpaRepository<EAAuthority, Long> {
    Optional<EAAuthority> findByAuthorityValue(String authorityValue);
}
