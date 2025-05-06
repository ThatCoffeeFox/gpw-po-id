package pl.gpwpoid.origin.account;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Date;

@Embeddable
public class AccountInfoId implements Serializable {

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public AccountInfoId() {}
    public AccountInfoId(Long accountId, OffsetDateTime updatedAt) {
        this.accountId = accountId;
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountInfoId that = (AccountInfoId) o;
        return accountId.equals(that.accountId) && updatedAt.equals(that.updatedAt);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(accountId, updatedAt);
    }

    public Long getAccountId() { return accountId; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
