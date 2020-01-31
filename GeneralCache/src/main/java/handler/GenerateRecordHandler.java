package handler;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lombok.NonNull;
import model.CustomerOffer;

import java.time.Instant;
import java.util.UUID;

public class GenerateRecordHandler implements RequestHandler<Void, Void> {
    @Override
    public Void handleRequest(Void aVoid, @NonNull final Context context) {
        DynamoDBMapper mapper = new DynamoDBMapper(AmazonDynamoDBClientBuilder.standard()
                .withRegion(getRegion())
                .build());

        CustomerOffer customerOffer = CustomerOffer.builder()
                .id(UUID.randomUUID().toString())
                .timeToLive(Instant.now().getEpochSecond() + 5000)
                .build();

        mapper.save(customerOffer,
                DynamoDBMapperConfig.builder()
                        .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride(getTableName()))
                        .build());
        return null;
    }


    private String getRegion() {
        return System.getenv("REGION");
    }

    private String getTableName() {
        return System.getenv("TableName");
    }
}
