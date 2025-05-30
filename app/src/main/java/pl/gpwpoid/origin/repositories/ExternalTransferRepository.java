package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.wallet.ExternalTransfer;

@Repository
public interface ExternalTransferRepository extends JpaRepository<ExternalTransfer, Long> {

}
