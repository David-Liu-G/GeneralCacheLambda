package handler;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.Record;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Handler implements RequestHandler<DynamodbEvent, Void> {
    private static final int PORT = 11211;
    private static final String END_POINT = "gen-el-ksqjvadabzg8.sbdldi.cfg.usw2.cache.amazonaws.com";

    @Override
    public Void handleRequest(@NonNull final DynamodbEvent dynamodbEvent, @NonNull final Context context) {
        MemcachedClient cachedClient;
        try {
            cachedClient = new MemcachedClient(new InetSocketAddress(END_POINT, PORT));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Map<String, AttributeValue>> recordList = Optional.ofNullable(dynamodbEvent.getRecords())
                .map(list -> list.stream()
                        .filter(Objects::nonNull)
                        .map(Record::getDynamodb)
                        .filter(Objects::nonNull)
                        .filter(this::dynamoDBRecordIsDeleted)
                        .map(StreamRecord::getOldImage)
                        .collect(Collectors.toList()))
                .orElse(ImmutableList.of());

        for (Map<String, AttributeValue> record : recordList) {
            String id = record.get("id").getS();
            if (cachedClient.get(id) == null) {
                cachedClient.add((id), 3600, record);
                System.out.println(String.format("id %s is in cache", id));
            } else {
                System.out.println(String.format("%s is expiring", id));
            }
        }

        return null;
    }

    private boolean dynamoDBRecordIsDeleted(@NonNull final StreamRecord record) {
        return record.getOldImage() != null && record.getNewImage() == null;
    }
}
