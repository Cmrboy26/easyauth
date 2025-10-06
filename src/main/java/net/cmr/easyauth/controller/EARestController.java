package net.cmr.easyauth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

import net.cmr.easyauth.entity.EALogin;
import net.cmr.easyauth.service.EALoginService;

public abstract class EARestController<L extends EALogin> {
    
    @Autowired(required = false)
    private EALoginService<L> loginService;

    private void register() {
        
        // Check if able to add, provides both REFRESH and ACCESS token as cookies, REFRESH token as body
        // Accessable by ANYONE
    }

    private void login() {
        // Check credentials, provides both REFRESH and ACCESS token as cookies, REFRESH token as body
        // Accessable by ANYONE
    }

    @PreAuthorize("hasAuthority('REFRESH')")
    private void refresh() {
        // Recieves REFRESH token, returns ACCESS token as body AND as cookie
        // Accessable by those with a REFRESH token
    }

    @PreAuthorize("hasAuthority('ACCESS') and hasRole('USER')")
    private void apiOperation() {
        // Recieves ACCESS token as header AND/OR cookie
        // Accessable by those with an ACCESS token
    }

}
