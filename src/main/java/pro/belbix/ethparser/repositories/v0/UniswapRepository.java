package pro.belbix.ethparser.repositories.v0;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.v0.UniswapDTO;

public interface UniswapRepository extends JpaRepository<UniswapDTO, String> {

    @Query(nativeQuery = true, value = ""
        + "select count(owner) from ( "
        + "     select distinct on (owner) "
        + "                                owner, "
        + "            last_value(owner_balance_usd) over w as balance "
        + "     from uni_tx "
        + "     where block_date <= :block_date "
        + "         window w as (PARTITION BY owner order by block_date desc) "
        + " ) t "
        + "where balance > 10")
    Integer fetchOwnerCount(@Param("block_date") long blockDate);

    UniswapDTO findFirstByCoinOrderByBlockDesc(String coin);

    UniswapDTO findFirstByBlockDateBeforeAndCoinOrderByBlockDesc(long blockDate, String coin);

    @Query("select sum(t.otherAmount) from UniswapDTO t "
        + "where t.coin = 'FARM' and t.owner = :owner and t.blockDate <= :from")
    List<Double> fetchAmountSumUsd(@Param("from") long from, @Param("owner") String owner, Pageable pageable);

    List<UniswapDTO> findAllByOwnerAndCoinOrderByBlockDate(String owner, String coin);

    @Query("select t from UniswapDTO t where "
        + "t.owner = :owner and t.coin = 'FARM' and t.blockDate > :from and t.blockDate <= :to order by t.blockDate asc")
    List<UniswapDTO> fetchAllByOwner(@Param("owner") String owner, @Param("from") long from, @Param("to") long to);

    List<UniswapDTO> findAllByOrderByBlockDate();

    List<UniswapDTO> findAllByBlockDateGreaterThanOrderByBlockDate(long from);

    @Query("select t from UniswapDTO t where "
        + "t.ownerBalance is null "
        + "or t.ownerBalanceUsd is null "
        + "order by t.blockDate asc")
    List<UniswapDTO> fetchAllWithoutOwnerBalance();

    @Query(nativeQuery = true, value =
        "select * from uni_tx t where t.coin = 'FARM' and t.block_date > :fromTs order by t.block_date")
    List<UniswapDTO> fetchAllFromBlockDate(@Param("fromTs") long fromTs);

    @Query(nativeQuery = true, value =
        "select * from uni_tx t where "
            + "t.coin = 'FARM' "
            + "and t.block_date > :from "
            + "and t.block_date <= :to "
            + "order by t.block_date")
    List<UniswapDTO> fetchAllByPeriod(@Param("from") long from, @Param("to") long to);

    @Query(nativeQuery = true, value = "" +
        "select FLOOR(MIN(block_date)/:period)*:period as timestamp,  " +
        "       SUBSTRING_INDEX(MIN(CONCAT(block_date, '_', last_price)), '_', -1) as open,  " +
        "       max(last_price) as high,  " +
        "       min(last_price) as low,  " +
        "       SUBSTRING_INDEX(MAX(CONCAT(block_date, '_', last_price)), '_', -1) as close,  " +
        "       sum(amount) as volume  " +
        "from uni_tx  " +
        "where coin = :coin and block_date between :startTime and :endTime " +
        "GROUP BY FLOOR(block_date/:period)  " +
        "order by timestamp;")
    List<OhlcProjection> fetchOHLCTransactions(
        @Param("coin") String coin,
        @Param("startTime") long startTime,
        @Param("endTime") long endTime,
        @Param("period") int period);

    interface OhlcProjection {

        long getTimestamp();

        double getOpen();

        double getHigh();

        double getLow();

        double getClose();

        double getVolume();
    }

}
