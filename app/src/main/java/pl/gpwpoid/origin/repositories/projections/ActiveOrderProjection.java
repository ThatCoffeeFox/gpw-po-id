package pl.gpwpoid.origin.repositories.projections;

public interface ActiveOrderProjection {
    Integer getOrderId();

    Integer getSharesLeft();

    java.util.Date getOrderStartDate();

    java.util.Date getOrderExpirationDate();

    java.math.BigDecimal getSharePrice();

    Integer getWalletId();

    Integer getCompanyId();

    Integer getSharesAmount();
}
