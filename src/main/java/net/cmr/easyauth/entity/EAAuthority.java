package net.cmr.easyauth.entity;

import org.springframework.security.core.GrantedAuthority;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class EAAuthority {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "authority_value", nullable = false)
    private String authorityValue;

    protected EAAuthority() { }

    public EAAuthority(String authorityValue) {
        this.authorityValue = authorityValue;
    }

    public Long getId() {
        return id;
    }
    
    public String getAuthorityValue() {
        return authorityValue;
    }

    public void setAuthorityValue(String authorityValue) {
        this.authorityValue = authorityValue;
    }

    public abstract GrantedAuthority generateAuthority();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EAAuthority that = (EAAuthority) obj;
        return authorityValue != null ? authorityValue.equals(that.authorityValue) : that.authorityValue == null;
    }

    @Override
    public int hashCode() {
        return authorityValue != null ? authorityValue.hashCode() : 0;
    }

}
