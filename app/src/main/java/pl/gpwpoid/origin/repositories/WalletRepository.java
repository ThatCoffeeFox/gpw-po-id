package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.wallet.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet,Long> {

}
