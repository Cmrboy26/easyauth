package net.cmr.easyauth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.cmr.easyauth.entity.Login;

@Repository
public interface LoginRepository extends JpaRepository<Login, Long> {
    Login findByUsernameOrEmail(String username, String email);
    String findPasswordById(Long id);
    @Query(nativeQuery = true, value = "SELECT * FROM login WHERE id BETWEEN :lowId AND :highId;")
    List<Login> getView(@Param("lowId") int lowId, @Param("highId") int highId);
}
