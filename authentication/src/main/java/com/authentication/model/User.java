package com.authentication.model;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @author Rohit.Kumar
 */
@Entity
@Table(name = "users")
public class User implements UserDetails {

    static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id", nullable = false, updatable = false)
    @ApiModelProperty(notes = "The database generated user, token and session mapping ID.")
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    @ApiModelProperty(notes = "user name")
    private String username;

    @Column(name = "password", nullable = false)
    @ApiModelProperty(notes = "User password")
    private String password;

    @Column(name = "enabled", nullable = false)
    @ApiModelProperty(notes = "Indicates whether the user is enabled or disabled. A disabled user cannot be authenticated.")
    private boolean enabled;

    @Column(name = "created_time", insertable=true, updatable=false)
    @ApiModelProperty(notes = "The database generated user, token and session mapping created time.")
    private LocalDateTime createdTime;

    @Column(name = "updated_time", insertable=false, updatable=true)
    @ApiModelProperty(notes = "The database generated user, token and session mapping updated time.")
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }
}
