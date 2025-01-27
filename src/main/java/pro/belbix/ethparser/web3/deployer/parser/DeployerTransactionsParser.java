package pro.belbix.ethparser.web3.deployer.parser;

import static pro.belbix.ethparser.web3.deployer.decoder.DeployerActivityEnum.CONTRACT_CREATION;

import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.codegen.FunctionWrapper;
import pro.belbix.ethparser.codegen.GeneratedContract;
import pro.belbix.ethparser.codegen.SimpleContractGenerator;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.v0.DeployerDTO;
import pro.belbix.ethparser.model.DeployerTx;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Subscriber;
import pro.belbix.ethparser.web3.deployer.db.DeployerDbService;
import pro.belbix.ethparser.web3.deployer.decoder.DeployerDecoder;

@Service
@Log4j2
public class DeployerTransactionsParser implements Web3Parser {

  public static final int LOG_LAST_PARSED_COUNT = 1_000;
  private static final AtomicBoolean run = new AtomicBoolean(true);
  private final DeployerDecoder deployerDecoder = new DeployerDecoder();
  private final Web3Subscriber web3Subscriber;
  private final BlockingQueue<Transaction> transactions = new ArrayBlockingQueue<>(100);
  private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
  private final DeployerDbService deployerDbService;
  private final EthBlockService ethBlockService;
  private final AppProperties appProperties;
  private final ParserInfo parserInfo;
  private final SimpleContractGenerator simpleContractGenerator;
  private final Web3Functions web3Functions;
  private long parsedTxCount = 0;
  private Instant lastTx = Instant.now();

  public DeployerTransactionsParser(
      Web3Subscriber web3Subscriber,
      DeployerDbService deployerDbService,
      EthBlockService ethBlockService,
      AppProperties appProperties, ParserInfo parserInfo,
      SimpleContractGenerator simpleContractGenerator,
      Web3Functions web3Functions) {
    this.web3Subscriber = web3Subscriber;
    this.deployerDbService = deployerDbService;
    this.ethBlockService = ethBlockService;
    this.appProperties = appProperties;
    this.parserInfo = parserInfo;
    this.simpleContractGenerator = simpleContractGenerator;
    this.web3Functions = web3Functions;
  }

  public void startParse() {
    log.info("Start parse Deployer transactions");
    parserInfo.addParser(this);
    web3Subscriber.subscribeOnTransactions(transactions);
    new Thread(
        () -> {
          while (run.get()) {
            Transaction transaction = null;
            try {
              transaction = transactions.poll(1, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }
            DeployerDTO dto = parseDeployerTransaction(transaction, appProperties.getNetwork());
            if (dto != null) {
              lastTx = Instant.now();
              try {
                boolean success = deployerDbService.save(dto);
                if (success) {
                  output.put(dto);
                }
              } catch (Exception e) {
                log.error("Can't save " + dto.toString(), e);
              }
            }
          }
        })
        .start();
  }

  public DeployerDTO parseDeployerTransaction(Transaction tx, String network) {
    if (tx == null) {
      return null;
    }

    incrementAndPrintCount(tx);
    DeployerTx deployerTx = deployerDecoder.decodeTransaction(tx, network);
    if (deployerTx == null) {
      return null;
    }
    TransactionReceipt transactionReceipt =
        web3Functions.fetchTransactionReceipt(tx.getHash(), network);
    deployerTx.setGasUsed(transactionReceipt.getGasUsed());
    deployerTx.setSuccess("0x1".equalsIgnoreCase(transactionReceipt.getStatus()));

    DeployerDTO dto = deployerTx.toDto(network);
    dto.setBlockDate(
        ethBlockService.getTimestampSecForBlock(tx.getBlockNumber().longValue(), network));

    if (CONTRACT_CREATION.name().equals(dto.getType())) {
      dto.setToAddress(transactionReceipt.getContractAddress());
    }
    GeneratedContract generatedContract = simpleContractGenerator.getContract(
        dto.getToAddress(), null, null, network);
    if (generatedContract != null) {
      dto.setName(generatedContract.getName());
      if (dto.getMethodName().startsWith("0x")) {
        FunctionWrapper functionWrapper = generatedContract.getFunction(dto.getMethodName());
        dto.setMethodName(functionWrapper.getFunction().getName());
      }
    }

    print(dto);

    return dto;
  }

  private void incrementAndPrintCount(Transaction tx) {
    parsedTxCount++;
    if (parsedTxCount % LOG_LAST_PARSED_COUNT == 0) {
      log.info("Deployer parsed " + parsedTxCount + ", last block: " + tx.getBlockNumber());
    }
  }

  private void print(DeployerDTO deployerDTO) {
    if (deployerDTO.getConfirmed() == 1) {
      log.info(deployerDTO.print() + " " + deployerDTO);
    } else {
      log.debug(deployerDTO.print() + " " + deployerDTO);
    }
  }

  public BlockingQueue<DtoI> getOutput() {
    return output;
  }

  @PreDestroy
  public void stop() {
    run.set(false);
  }

  @Override
  public Instant getLastTx() {
    return lastTx;
  }
}
