package pro.belbix.ethparser.repositories.v0;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.v0.HardWorkDTO;

public interface HardWorkRepository extends JpaRepository<HardWorkDTO, String> {

    @Query("select t from HardWorkDTO t where "
        + "t.fullRewardUsd > :minAmount "
        + "and t.network = :network")
    Page<HardWorkDTO> fetchPages(
        @Param("minAmount") double vault,
        @Param("network") String network,
        Pageable pageable);

    @Query("select t from HardWorkDTO t where "
        + "t.vault = :vault "
        + "and t.fullRewardUsd > :minAmount "
        + "and t.network = :network")
    Page<HardWorkDTO> fetchPagesByVault(
        @Param("vault") String vault,
        @Param("network") String network,
        @Param("minAmount") double minAmount,
        Pageable pageable);

    HardWorkDTO findFirstByNetworkOrderByBlockDateDesc(String network);

    @Query("select t from HardWorkDTO t where "
        + "t.blockDate between :from and :to "
        + "and t.network = :network "
        + "order by t.blockDate")
    List<HardWorkDTO> fetchAllInRange(@Param("from") long from,
        @Param("to") long to,
        @Param("network") String network
    );

    @Query(""
        + "select sum(t.fullRewardUsd) from HardWorkDTO t "
        + "where t.vault = :vault "
        + "and t.blockDate <= :blockDate "
        + "and t.network = :network")
    Double getSumForVault(
        @Param("vault") String vault,
        @Param("blockDate") long blockDate,
        @Param("network") String network
    );

    @Query("select sum(t.perc) from HardWorkDTO t where "
        + "t.vault = :vault "
        + "and t.blockDate <= :to "
        + "and t.network = :network")
    List<Double> fetchPercentForPeriod(
        @Param("vault") String vault,
        @Param("to") long to,
        @Param("network") String network,
        Pageable pageable);

    @Query("select sum(t.fullRewardUsd) from HardWorkDTO t where "
        + "t.vault = :vault "
        + "and t.blockDate between :from and :to "
        + "and t.network = :network")
    List<Double> fetchProfitForPeriod(
        @Param("vault") String vault,
        @Param("from") long from,
        @Param("to") long to,
        @Param("network") String network,
        Pageable pageable);

    @Query("select sum(t.fullRewardUsd) from HardWorkDTO t where "
        + "t.blockDate <= :to "
        + "and t.network = :network")
    List<Double> fetchAllProfitAtDate(
        @Param("to") long to,
        @Param("network") String network,
        Pageable pageable);

    @Query("select sum(t.fullRewardUsd) from HardWorkDTO t where "
        + "t.blockDate between :from and :to "
        + "and t.network = :network")
    List<Double> fetchAllProfitForPeriod(
        @Param("from") long from,
        @Param("to") long to,
        @Param("network") String network,
        Pageable pageable);

    @Query("select sum(t.farmBuyback) from HardWorkDTO t where "
        + " t.blockDate < :to "
        + "and t.network = :network")
    List<Double> fetchAllBuybacksAtDate(
        @Param("to") long to,
        @Param("network") String network,
        Pageable pageable);

    @Query("select count(t) from HardWorkDTO t where "
        + "t.vault = :vault "
        + "and t.blockDate <= :blockDate "
        + "and t.network = :network")
    Integer countAtBlockDate(
        @Param("vault") String vault,
        @Param("network") String network,
        @Param("blockDate") long blockDate);

    @Query("select sum(t.savedGasFees) from HardWorkDTO t where "
        + "t.vault = :vault "
        + "and t.network = :network "
        + "and t.blockDate < :blockDate")
    Double sumSavedGasFees(
        @Param("vault") String vault,
        @Param("network") String network,
        @Param("blockDate") long blockDate
    );

    @Query("select t from HardWorkDTO t where "
        + "t.vault = :vault "
        + "and t.network = :network "
        + "and t.blockDate between :startTime and :endTime "
        + "order by t.blockDate")
    List<HardWorkDTO> findAllByVaultOrderByBlockDate(
        @Param("vault") String vault,
        @Param("network") String network,
        @Param("startTime") long startTime,
        @Param("endTime") long endTime);

    // excellent explanation https://stackoverflow.com/a/7630564/6537367
    @Query(nativeQuery = true, value = "" +
        "select distinct on (vault) "
        + "    last_value(id) over w                    as id, "
        + "    vault, "
        + "    last_value(block) over w                 as block, "
        + "    last_value(block_date) over w            as block_date, "
        + "    last_value(network) over w               as network, "
        + "    last_value(share_change) over w          as share_change, "
        + "    last_value(full_reward_usd) over w       as full_reward_usd, "
        + "    last_value(full_reward_usd_total) over w as full_reward_usd_total, "
        + "    last_value(tvl) over w                   as tvl, "
        + "    last_value(all_profit) over w            as all_profit, "
        + "    last_value(period_of_work) over w        as period_of_work, "
        + "    last_value(ps_period_of_work) over w     as ps_period_of_work, "
        + "    last_value(perc) over w                  as perc, "
        + "    last_value(ps_tvl_usd) over w            as ps_tvl_usd, "
        + "    last_value(ps_apr) over w                as ps_apr, "
        + "    last_value(apr) over w                   as apr, "
        + "    last_value(weekly_profit) over w         as weekly_profit, "
        + "    last_value(weekly_all_profit) over w     as weekly_all_profit, "
        + "    last_value(farm_buyback) over w          as farm_buyback, "
        + "    last_value(farm_buyback_sum) over w      as farm_buyback_sum, "
        + "    last_value(calls_quantity) over w        as calls_quantity, "
        + "    last_value(pool_users) over w            as pool_users, "
        + "    last_value(saved_gas_fees) over w        as saved_gas_fees, "
        + "    last_value(saved_gas_fees_sum) over w    as saved_gas_fees_sum, "
        + "    last_value(fee) over w                   as fee, "
        + "    last_value(weekly_average_tvl) over w    as weekly_average_tvl, "
        + "    last_value(farm_buyback_eth) over w      as farm_buyback_eth, "
        + "    last_value(fee_eth) over w               as fee_eth, "
        + "    last_value(gas_used) over w              as gas_used, "
        + "    last_value(idle_time) over w             as idle_time, "
        + "    last_value(invested) over w              as invested, "
        + "    last_value(investment_target) over w     as investment_target, "
        + "    last_value(farm_price) over w            as farm_price, "
        + "    last_value(eth_price) over w             as eth_price, "
        + "    last_value(buy_back_rate) over w             as buy_back_rate, "
        + "    last_value(profit_sharing_rate) over w             as profit_sharing_rate, "
        + "    last_value(auto_stake) over w             as auto_stake "
        + "from hard_work where network = :network "
        + "    window w as (PARTITION BY vault order by block_date desc) "
        + "order by vault")
    List<HardWorkDTO> fetchLatest(@Param("network") String network);

    @Query(nativeQuery = true, value = ""
        + "select sum(t.saved_gas_fees_sum) gas_saved "
        + "from ( "
        + "         select distinct on (vault) "
        + "                last_value(saved_gas_fees_sum) over w as saved_gas_fees_sum "
        + "         from hard_work where network = :network "
        + "             window w as (PARTITION BY vault order by block_date desc) "
        + "     ) t")
    Double fetchLastGasSaved(@Param("network") String network);

    @Query(nativeQuery = true, value = "select block_date from hard_work "
        + "where vault = :vault "
        + "and block_date < :block_date "
        + "and network = :network "
        + "order by block_date desc limit 1")
    Long fetchPreviousBlockDateByVaultAndDate(
        @Param("vault") String vault,
        @Param("network") String network,
        @Param("block_date") long blockDate
    );
}
