package pro.belbix.ethparser.repositories.v0;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class HarvestTvlRepositoryTest {

    @Autowired
    private HarvestTvlRepository harvestTvlRepository;

    @Test
    public void findAllByOrderByCalculateTime() {
        assertNotNull(harvestTvlRepository.findAllByNetworkOrderByCalculateTime(ETH_NETWORK));
    }

    @Test
    public void getHistoryOfAllTvl() {
        assertNotNull(harvestTvlRepository.getHistoryOfAllTvl(0, Long.MAX_VALUE, ETH_NETWORK));
    }
}
