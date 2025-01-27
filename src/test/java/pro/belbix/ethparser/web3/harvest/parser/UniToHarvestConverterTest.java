package pro.belbix.ethparser.web3.harvest.parser;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.TestUtils.numberFormat;
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
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.dto.v0.UniswapDTO;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.prices.PriceProvider;
import pro.belbix.ethparser.web3.uniswap.parser.UniswapLpLogParser;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class UniToHarvestConverterTest {

  @Autowired
  private UniswapLpLogParser uniswapLpLogParser;
  @Autowired
  private Web3Functions web3Functions;
  @Autowired
  private PriceProvider priceProvider;
  @Autowired
  private UniToHarvestConverter uniToHarvestConverter;
  @Autowired
  private ContractLoader contractLoader;

  @BeforeEach
  public void setUp() throws Exception {
    contractLoader.load();
  }


  @Test
  @Disabled
    public void convertUNI_LP_USDC_FARM_ADD() {
        UniswapDTO dto = uniswapParseTest(
                "0x514906fc121c7878424a5c928cad1852cc545892",
                10777206,
                2,
                "0xd00b70b93a3df0910bbb426df4fdf1374d206339d9b3334075c691e086a45177_107",
                "0xe6765078ca6e641ec9637cd145ecfad30aa53a4a",
                "58,70106387",
                "ADD",
            "USDC",
                "15800,00000000",
                "269,16036881"
        );

        HarvestDTO harvestDTO = uniToHarvestConverter.convert(dto);
        assertNotNull(harvestDTO);
        assertAll(
                () -> assertEquals("Amount", "58,70106387", String.format("%.8f", harvestDTO.getAmount())),
                () -> assertEquals("Method", "Deposit", harvestDTO.getMethodName()),
                () -> assertEquals("Vault", "UNI_LP_USDC_FARM", harvestDTO.getVault()),
                () -> assertEquals("UsdAmount", "31600", String.format("%.0f", harvestDTO.getUsdAmount().doubleValue())),
                () -> assertEquals("LastUsdTvl", "9657721", String.format("%.0f", harvestDTO.getLastUsdTvl())),
                () -> assertEquals("LpStat", "{\"coin1\":\"FARM\",\"coin2\":\"USDC\",\"amount1\":20641.504241330007,\"amount2\":4828860.303375672}",
                        harvestDTO.getLpStat()),
                () -> assertEquals("LastTvl", "0,30169333", String.format("%.8f",  harvestDTO.getLastTvl()))
        );
    }

    private UniswapDTO uniswapParseTest(
        String contract,
        int onBlock,
        int logId,
        String id,
        String owner,
        String amount,
        String type,
        String otherCoin,
        String otherAmount,
        String lastPrice
    ) {
      amount = numberFormat(amount);
      otherAmount = numberFormat(otherAmount);
      lastPrice = numberFormat(lastPrice);
      List<LogResult> logResults = web3Functions
          .fetchContractLogs(singletonList(contract), onBlock, onBlock, ETH_NETWORK);
      assertTrue("Log smaller then necessary " + logResults.size(), logId < logResults.size());
      UniswapDTO dto = uniswapLpLogParser.parseUniswapLog((Log) logResults.get(logId).get());
      assertDto(dto,
          id,
          owner,
          amount,
          type,
          otherCoin,
          otherAmount,
            lastPrice
        );
        return dto;
    }

    private void assertDto(UniswapDTO dto,
                           String id,
                           String owner,
                           String amount,
                           String type,
                           String otherCoin,
                           String otherAmount,
                           String lastPrice) {
        assertNotNull(dto, "Dto is null");
        assertAll(() -> assertEquals("owner", owner, dto.getOwner()),
                () -> assertEquals("Id", id, dto.getId()),
                () -> assertEquals("Amount", amount, String.format("%.8f", dto.getAmount())),
                () -> assertEquals("Type", type, dto.getType()),
                () -> assertEquals("OtherCoin", otherCoin, dto.getOtherCoin()),
                () -> assertEquals("otherAmount", otherAmount, String.format("%.8f", dto.getOtherAmount())),
                () -> assertEquals("lastPrice", lastPrice, String.format("%.8f", dto.getLastPrice()))
        );
    }

}
