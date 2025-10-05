package net.cmr.easyauth.service;

import java.util.InputMismatchException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.MappedSuperclass;
import net.cmr.easyauth.entity.EALogin;
import net.cmr.easyauth.respository.EALoginRepository;
import net.cmr.easyauth.util.JwtUtil;

@MappedSuperclass
public abstract class EALoginService<L extends EALogin> {

    @Autowired(required = false)
    private EALoginRepository<L> loginRepository;

    /**
     * NOTE: pl
     * @param validAccessJwt the access JWT of the desired user, may be null
     * @param validRefreshJwt the refresh JWT of the desired user, may be null
     * @return an optional if the database was pinged, null if both tokens are null.
     */
    @Transactional(readOnly = true)
    public Optional<L> getUserFromJwt(String validAccessJwt, String validRefreshJwt) {
        Long accessID = null;
        Long refreshID = null;
        if (validAccessJwt == null && validRefreshJwt == null) {
            return null;
        }
        Long targetId = null;
        if (validAccessJwt != null) {
            accessID = JwtUtil.getId(validAccessJwt);
            targetId = accessID;
        }
        if (validRefreshJwt != null) {
            refreshID = JwtUtil.getId(validRefreshJwt);
            targetId = refreshID;
        }
        // If both aren't the same, there's likely tampering, throw an error
        // Very very very unlikely I think
        if (accessID != null && refreshID != null && accessID != refreshID) {
            throw new InputMismatchException("Underlying IDs don't match.");
        }
        if (targetId == null) {
            throw new NullPointerException("Target Id is null, likely a programming error");
        }
        return loginRepository.findById(targetId);
    }

}
