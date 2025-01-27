package pro.belbix.ethparser.repositories.eth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.entity.contracts.ContractEntity;

public interface ContractRepository extends JpaRepository<ContractEntity, Integer> {

    @Query("select t from ContractEntity t "
        + "where t.address = :address and t.network = :network")
    ContractEntity findFirstByAddress(
        @Param("address") String address,
        @Param("network") String network
    );

}
