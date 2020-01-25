package helloworld;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import model.HandlerRequest;
import model.HandlerResponse;
import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Handler implements RequestHandler<HandlerRequest, HandlerResponse> {
    private static final int PORT = 11211;
    private static final String END_POINT = "gen-el-ksqjvadabzg8.sbdldi.cfg.usw2.cache.amazonaws.com";

    @Override
    public HandlerResponse handleRequest(HandlerRequest handlerRequest, Context context) {
        LambdaLogger logger = context.getLogger();
        MemcachedClient cachedClient;
        try {
            cachedClient = new MemcachedClient(new InetSocketAddress(END_POINT, PORT));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String value = "123";

        if (cachedClient.get(handlerRequest.getInput()) == null) {
            cachedClient.set(handlerRequest.getInput(), 900, value);
            logger.log("set value to cache");
        } else {
            value = (String) cachedClient.get(handlerRequest.getInput());
            logger.log(String.format("Got value from cache %s", value));
        }
        return HandlerResponse.builder()
                .output(value)
                .build();
    }
}
