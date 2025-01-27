package pro.belbix.ethparser.web3.harvest.decoder;

import java.math.BigInteger;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.model.EthTransactionI;
import pro.belbix.ethparser.model.HardWorkTx;
import pro.belbix.ethparser.web3.MethodDecoder;

@SuppressWarnings({"unchecked", "rawtypes"})
@Log4j2
public class HardWorkLogDecoder extends MethodDecoder {

  public HardWorkTx decode(Log ethLog) {
    if (!isValidLog(ethLog)) {
      return null;
    }
    List<TypeReference<Type>> parameters;
    String methodName;
    try {
      String methodId = parseMethodId(ethLog)
          .orElseThrow(() -> new IllegalStateException("Unknown topic " + ethLog));
      methodName = methodNamesByMethodId.get(methodId);
      parameters = findParameters(methodId)
          .orElseThrow(() -> new IllegalStateException("Not found parameters for " + methodId));
    } catch (IllegalStateException e) {
      return null;
    }

    List<Type> types = extractLogIndexedValues(ethLog.getTopics(), ethLog.getData(), parameters);
    HardWorkTx tx = new HardWorkTx();
    tx.setLogId(ethLog.getLogIndex().toString());
    tx.setHash(ethLog.getTransactionHash());
    tx.setMethodName(methodName);
    tx.setBlock(ethLog.getBlockNumber().longValue());
    enrich(types, tx);
    return tx;
  }

  private boolean isValidLog(Log ethLog) {
    return ethLog != null && !ethLog.getTopics().isEmpty();
  }

  private void enrich(List<Type> types, HardWorkTx tx) {
    if ("SharePriceChangeLog".equals(tx.getMethodName())) {
      tx.setVault((String) types.get(0).getValue());
      tx.setStrategy((String) types.get(1).getValue());
      tx.setOldSharePrice((BigInteger) types.get(2).getValue());
      tx.setNewSharePrice((BigInteger) types.get(3).getValue());
      tx.setBlockDate(((BigInteger) types.get(4).getValue()).longValue());
    } else if ("ProfitLogInReward".equals(tx.getMethodName())) {
      tx.setProfitAmount((BigInteger) types.get(0).getValue());
      tx.setFeeAmount((BigInteger) types.get(1).getValue());
    } else if ("RewardAdded".equals(tx.getMethodName())) {
      tx.setReward((BigInteger) types.get(0).getValue());
    }
  }

  @Override
  public EthTransactionI mapTypesToModel(List<Type> types, String methodID,
      Transaction transaction) {
    throw new UnsupportedOperationException();
  }
}
