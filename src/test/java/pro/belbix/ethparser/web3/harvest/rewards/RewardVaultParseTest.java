package pro.belbix.ethparser.web3.harvest.rewards;

import static java.util.Collections.singletonList;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.model.HarvestTx;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.harvest.decoder.VaultActionsLogDecoder;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class RewardVaultParseTest {

  @Autowired
  private Web3Functions web3Functions;
  @Autowired
  private ContractLoader contractLoader;

  private final VaultActionsLogDecoder vaultActionsLogDecoder = new VaultActionsLogDecoder();

   @BeforeEach
  public void setUp() throws Exception {
    contractLoader.load();
  }

  @Test
  @Disabled
  public void ycrvTest() {
    List<LogResult> logResults = web3Functions
        .fetchContractLogs(singletonList("0x6D1b6Ea108AA03c6993d8010690264BA96D349A8"), null, null, ETH_NETWORK);
    for (LogResult logResult : logResults) {
      Log ethLog = (Log) logResult.get();
      HarvestTx tx = vaultActionsLogDecoder.decode(ethLog);
      if ("RewardAdded".equals(tx.getMethodName())) {
                System.out.println(ethLog);
            }
        }
    }

    @Test
    public void ycrvTest_RewardDenied() {
        List<LogResult> logResults = web3Functions
            .fetchContractLogs(singletonList("0x6D1b6Ea108AA03c6993d8010690264BA96D349A8"), 11413701, 11413701, ETH_NETWORK);
        for (LogResult logResult : logResults) {
            Log ethLog = (Log) logResult.get();
            vaultActionsLogDecoder.decode(ethLog);
        }
    }
}
