package pro.belbix.ethparser.web3.abi;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.web3j.abi.datatypes.Event;
import org.web3j.protocol.core.RemoteFunctionCall;
import pro.belbix.ethparser.web3.MethodDecoder;
import pro.belbix.ethparser.web3.abi.generated.WrapperMapper;

@Log4j2
public class WrapperReader {

    private static Map<String, Event> eventsMap;

    public static void collectMethods(Class<?> clazz) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            for (MethodDescriptor methodDescriptor : beanInfo.getMethodDescriptors()) {
                // only static, view and pure
                if (!methodDescriptor.getName().startsWith("call_")) {
                    continue;
                }
                // only remote calls
                if (!RemoteFunctionCall.class
                    .equals(methodDescriptor.getMethod().getReturnType())) {
                    continue;
                }
                // only getters
                if (methodDescriptor.getMethod().getParameterCount() != 0) {
                    continue;
                }
                log.info("method {}", methodDescriptor.getName());
            }
        } catch (Exception e) {
            log.error("Error collect methods", e);
        }
    }

    public static List<Event> collectEvents(Class<?> clazz) {
        List<Event> events = new ArrayList<>();
        try {
            for(Field field : clazz.getDeclaredFields()) {
                if(!field.getName().endsWith("_EVENT")
                    || field.getType() != Event.class) {
                    continue;
                }
                log.info("desc "+ field.getName());
                events.add((Event) field.get(null));
            }
        } catch (Exception e) {
            log.error("Error collect events", e);
        }
        return events;
    }

    public static Event findEventByHex(String hex) {
        if(eventsMap == null) {
            initEventsMap();
        }
        return eventsMap.get(hex);
    }

    private static void initEventsMap() {
        eventsMap = new HashMap<>();
        for (Class<?> clazz : WrapperMapper.contractToWrapper.values()) {
            for (Event event : collectEvents(clazz)) {
                String methodHex = MethodDecoder
                    .createMethodFullHex(event.getName(), event.getParameters());
                eventsMap.put(methodHex, event);
            }
        }
    }

}
