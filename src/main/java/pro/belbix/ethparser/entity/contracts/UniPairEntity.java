package pro.belbix.ethparser.entity.contracts;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "eth_uni_pairs", indexes = {
    @Index(name = "idx_eth_uni_pairs", columnList = "contract")
})
@Data
public class UniPairEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Long updatedBlock;
    private int type;
    private Long decimals;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contract", unique = true)
    @Fetch(FetchMode.JOIN)
    private ContractEntity contract;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "key_token")
    @Fetch(FetchMode.JOIN)
    private TokenEntity keyToken;

    @ManyToOne(fetch = FetchType.EAGER)
    private ContractEntity token0;

    @ManyToOne(fetch = FetchType.EAGER)
    private ContractEntity token1;

}
