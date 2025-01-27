package pro.belbix.ethparser.utils;

import static pro.belbix.ethparser.model.UniswapTx.ADD_LIQ;
import static pro.belbix.ethparser.model.UniswapTx.REMOVE_LIQ;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.FARM_TOKEN;

import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pro.belbix.ethparser.dto.v0.HardWorkDTO;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.dto.v0.ImportantEventsDTO;
import pro.belbix.ethparser.dto.v0.PriceDTO;
import pro.belbix.ethparser.dto.v0.UniswapDTO;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.harvest.decoder.VaultActionsLogDecoder;

public class MockUtils {
    private static final ContractUtils contractUtils = ContractUtils.getInstance(ETH_NETWORK);
    private static final List<String> harvestMethods =
        new ArrayList<>(new VaultActionsLogDecoder().getMethodNamesByMethodId().values());

    public static UniswapDTO createUniswapDTO(long seed) {
        double currentCount = seed * new Random().nextDouble();
        UniswapDTO uniswapDTO = new UniswapDTO();
        uniswapDTO.setId("0x" + (seed * 1000000));
        uniswapDTO.setBlock(BigInteger.valueOf(seed * 1000000));
        uniswapDTO.setAmount(currentCount);
        uniswapDTO.setOtherAmount(currentCount);
        uniswapDTO.setCoin("FARM");
        uniswapDTO.setOtherCoin("ETH");
        uniswapDTO.setHash("0x" + seed);
        uniswapDTO.setType(new Random().nextBoolean() ?
            new Random().nextBoolean() ? "BUY" : "SELL" :
            new Random().nextBoolean() ? ADD_LIQ : REMOVE_LIQ);
        uniswapDTO.setPrice(currentCount);
        uniswapDTO.setConfirmed(new Random().nextBoolean());
        uniswapDTO.setLastGas(currentCount / 6);
        uniswapDTO.setBlockDate(Instant.now().plus(seed, ChronoUnit.MINUTES).getEpochSecond());
        return uniswapDTO;
    }

    public static HarvestDTO createHarvestDTO(long seed) {
        double currentCount = seed * new Random().nextDouble();
        HarvestDTO harvestDTO = new HarvestDTO();
        harvestDTO.setBlock(seed * 1000000);
        harvestDTO.setAmount(currentCount * 10000);
        harvestDTO.setNetwork("eth");
        harvestDTO.setUsdAmount((long) currentCount * 100);
        harvestDTO.setVault(new ArrayList<>(contractUtils.vaultNames())
            .get(new Random().nextInt(contractUtils.vaultNames().size() - 1)));
        harvestDTO.setId("0x" + (seed * 1000000));
        harvestDTO.setHash("0x" + seed);
        harvestDTO.setMethodName(harvestMethods.get(new Random().nextInt(harvestMethods.size() - 1)));
        harvestDTO.setLastUsdTvl(currentCount * 1000000);
        harvestDTO.setConfirmed(1);
        harvestDTO.setLastGas(currentCount / 6);
        harvestDTO.setBlockDate(Instant.now().plus(seed, ChronoUnit.MINUTES).getEpochSecond());
        return harvestDTO;
    }

    public static HardWorkDTO createHardWorkDTO(long seed) {
        HardWorkDTO hardWorkDTO = new HardWorkDTO();
        hardWorkDTO.setId("0x" + (seed * 1000000));
        hardWorkDTO.setBlock(seed * 1000000);
        hardWorkDTO.setVault(new ArrayList<>(contractUtils.vaultNames())
            .get(new Random().nextInt(contractUtils.vaultNames().size() - 1)));
        hardWorkDTO.setBlockDate(Instant.now().plus(seed, ChronoUnit.MINUTES).getEpochSecond());
        hardWorkDTO.setShareChange(seed / 1000.0);
        hardWorkDTO.setFullRewardUsd(seed / 69.0);
        hardWorkDTO.setFullRewardUsdTotal(seed);
        hardWorkDTO.setTvl(seed * 60);
        hardWorkDTO.setPerc((double) seed / 633.0);
        hardWorkDTO.setPsApr((double) seed / 63.0);
        hardWorkDTO.setNetwork("net");
        return hardWorkDTO;
    }

    public static ImportantEventsDTO createImportantEventsDTO(long seed) {
        ImportantEventsDTO dto = new ImportantEventsDTO();
        dto.setId(seed + "id");
        dto.setHash(seed + "hash");
        dto.setBlock(seed * 1000000);
        dto.setBlockDate(Instant.now().getEpochSecond());
        dto.setEvent("StrategyChanged");
        dto.setOldStrategy("oldsStr");
        dto.setNewStrategy("newStr");
        dto.setVault("vault");
        dto.setMintAmount(seed * 0.3);
        dto.setInfo("{}");
        dto.setNetwork("eth");
        return dto;
    }

    public static PriceDTO createPriceDTO(long seed) {
        double randomDouble = seed * new Random().nextDouble();
        PriceDTO.PriceDTOBuilder dto = PriceDTO.builder()
            .id(seed + "id")
            .block(seed * 1000000)
            .blockDate(Instant.now().getEpochSecond())
            .network("eth")
            .tokenAmount(randomDouble)
            .otherTokenAmount(randomDouble)
            .price(randomDouble)
            .buy(new Random().nextBoolean() ? 1 : 0)
            .lpTotalSupply(randomDouble)
            .lpToken0Pooled(randomDouble)
            .lpToken1Pooled(randomDouble);
        if (new Random().nextBoolean()) {
            dto.token("FARM")
                .tokenAddress(FARM_TOKEN)
                .otherToken("ETH")
                .otherTokenAddress("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2")
                .source("UNI_LP_WETH_FARM")
                .sourceAddress("0x56feaccb7f750b997b36a68625c7c596f0b41a58");
        } else {
            dto.token("ETH")
                .tokenAddress("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2")
                .otherToken("USDC")
                .otherTokenAddress("0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48")
                .source("UNI_LP_USDC_ETH")
                .sourceAddress("0xB4e16d0168e52d35CaCD2c6185b44281Ec28C9Dc");
        }
        return dto.build();
    }

}
