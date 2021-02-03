package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.eth.ContractEntity;
import pro.belbix.ethparser.entity.eth.PoolEntity;

public interface PoolRepository extends JpaRepository<PoolEntity, Integer> {

    PoolEntity findFirstByAddress(ContractEntity address);

}
