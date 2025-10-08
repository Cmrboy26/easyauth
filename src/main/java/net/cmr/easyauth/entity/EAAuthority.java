package net.cmr.easyauth.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@Entity
public class EAAuthority {
    
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

    public GrantedAuthority generateAuthority() {
        return new SimpleGrantedAuthority(authorityValue);
    }

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

    @Override
    public String toString() {
        return authorityValue;
    }

}
