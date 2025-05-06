package pl.gpwpoid.origin.account;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.Set;


@Entity
@Table(name = "accounts")
public class Account {

    public enum UserRole {
        admin,
        user
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Enumerated(EnumType.STRING) 
    @Column(name = "role", nullable = false, columnDefinition = "user_role") 
    private UserRole role;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<AccountInfo> infos;

    public Long getAccountId() {
        return accountId;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Set<AccountInfo> getInfos() {
        return infos;
    }

    public void setInfos(Set<AccountInfo> infos) {
        this.infos = infos;
    }
}
