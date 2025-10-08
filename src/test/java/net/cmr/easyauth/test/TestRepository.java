package net.cmr.easyauth.test;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import net.cmr.easyauth.respository.EALoginRepository;

@Repository
public interface TestRepository extends EALoginRepository<TestLogin> {
   Optional<TestLogin> findByEmail(String email);
}
