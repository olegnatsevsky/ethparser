package pro.belbix.ethparser.entity.eth;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "eth_vaults", indexes = {
    @Index(name = "idx_eth_vaults", columnList = "address")
})
@Data
public class VaultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "address", unique = true)
    private ContractEntity address;
    private Long updatedBlock;

    // contract info
    @ManyToOne
    private ContractEntity controller;
    @ManyToOne
    private ContractEntity governance;
    @ManyToOne
    private ContractEntity strategy;
    @ManyToOne
    private ContractEntity underlying;
    private String name;
    private String symbol;
    private Long decimals;
    private Long underlyingUnit;

}
