package net.cmr.easyauth.test;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import net.cmr.easyauth.service.EALoginService;
import net.cmr.easyauth.util.NonNullMap;

@Service
@Profile("test")
public class TestLoginService extends EALoginService<TestLogin> {

    @Override
    protected TestLogin createNewInstance(NonNullMap<String, String> registerParams) {
        return new TestLogin(registerParams);
    }

    @Override
    public boolean overrideAreCredentialsUnique(NonNullMap<String, String> registerParams) {
        return registerParams.getNullable("email") == null || getLoginRepository(TestRepository.class).findByEmail(registerParams.get("email")).isEmpty();
    }

    public boolean sendEmail(TestLogin login) {
        // Email sending logic here...
        return true;
    }

}
