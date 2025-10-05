package net.cmr.easyauth.test;

import org.springframework.stereotype.Repository;

import net.cmr.easyauth.respository.EALoginRepository;

@Repository
public interface TestRepository extends EALoginRepository<TestLogin> {
   
}
