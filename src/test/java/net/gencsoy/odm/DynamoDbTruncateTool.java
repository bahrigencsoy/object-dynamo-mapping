package net.gencsoy.odm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A utility class to truncate (delete all items from) a DynamoDB table.
 * * Usage:
 * DynamoDbClient client = DynamoDbClient.create();
 * DynamoDbTruncateTool.truncateTable(client, "MyTableName");
 */
public class DynamoDbTruncateTool {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDbTruncateTool.class);
    private static final int BATCH_SIZE = 25;

    /**
     * Deletes all items in the specified DynamoDB table.
     *
     * @param ddb       The instantiated DynamoDbClient.
     * @param tableName The name of the table to truncate.
     */
    public static void truncateTable(DynamoDbClient ddb, String tableName) {
        logger.info("Starting truncation for table: {}", tableName);

        // 1. Describe the table to find the Primary Key structure (Partition + optional Sort key)
        TableDescription tableInfo = getTableDescription(ddb, tableName);
        List<String> keyAttributeNames = getKeyAttributeNames(tableInfo);

        logger.info("Identified keys: {}", keyAttributeNames);

        // 2. Scan the table to find all items, projecting only the Key attributes to save bandwidth/RCUs
        Map<String, AttributeValue> lastEvaluatedKey = null;
        long totalDeleted = 0;

        do {
            ScanRequest.Builder scanBuilder = ScanRequest.builder()
                    .tableName(tableName)
                    .projectionExpression(String.join(",", keyAttributeNames))
                    .limit(200); // Scan in manageable chunks

            if (lastEvaluatedKey != null && !lastEvaluatedKey.isEmpty()) {
                scanBuilder.exclusiveStartKey(lastEvaluatedKey);
            }

            ScanResponse scanResponse = ddb.scan(scanBuilder.build());
            List<Map<String, AttributeValue>> items = scanResponse.items();

            if (!items.isEmpty()) {
                // 3. Delete the fetched items in batches
                processBatchDelete(ddb, tableName, items, keyAttributeNames);
                totalDeleted += items.size();
                logger.info("Deleted {} items so far...", totalDeleted);
            }

            lastEvaluatedKey = scanResponse.lastEvaluatedKey();

        } while (lastEvaluatedKey != null && !lastEvaluatedKey.isEmpty());

        logger.info("Truncation complete. Total items deleted: {}", totalDeleted);
    }

    /**
     * Helper to fetch table description.
     */
    private static TableDescription getTableDescription(DynamoDbClient ddb, String tableName) {
        DescribeTableResponse response = ddb.describeTable(DescribeTableRequest.builder()
                .tableName(tableName)
                .build());
        return response.table();
    }

    /**
     * Extracts the names of the Partition Key and Sort Key (if it exists) from the table description.
     */
    private static List<String> getKeyAttributeNames(TableDescription tableInfo) {
        List<String> keys = new ArrayList<>();
        for (KeySchemaElement element : tableInfo.keySchema()) {
            // We need both HASH (Partition) and RANGE (Sort) keys to identify an item uniquely
            keys.add(element.attributeName());
        }
        return keys;
    }

    /**
     * Batches items into groups of 25 and sends BatchWriteItem requests.
     */
    private static void processBatchDelete(DynamoDbClient ddb, String tableName,
                                           List<Map<String, AttributeValue>> items,
                                           List<String> keyAttributeNames) {

        List<WriteRequest> writeRequests = new ArrayList<>();

        for (Map<String, AttributeValue> item : items) {
            // Construct the Key map for deletion (filter the item to only include key attributes)
            // Although the scan only returned keys, this is a safety measure.
            Map<String, AttributeValue> keyMap = item.entrySet().stream()
                    .filter(e -> keyAttributeNames.contains(e.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            DeleteRequest deleteRequest = DeleteRequest.builder().key(keyMap).build();
            writeRequests.add(WriteRequest.builder().deleteRequest(deleteRequest).build());

            // If we hit the batch limit, execute
            if (writeRequests.size() == BATCH_SIZE) {
                executeBatchWrite(ddb, tableName, writeRequests);
                writeRequests.clear();
            }
        }

        // Process any remaining items
        if (!writeRequests.isEmpty()) {
            executeBatchWrite(ddb, tableName, writeRequests);
        }
    }

    /**
     * Executes the BatchWriteItem request and handles UnprocessedItems (throttling).
     */
    private static void executeBatchWrite(DynamoDbClient ddb, String tableName, List<WriteRequest> writeRequests) {
        BatchWriteItemRequest batchRequest = BatchWriteItemRequest.builder()
                .requestItems(Map.of(tableName, writeRequests))
                .build();

        BatchWriteItemResponse response = ddb.batchWriteItem(batchRequest);

        // Handle Unprocessed Items (Back-off logic could be added here, currently simple recursion)
        while (!response.unprocessedItems().isEmpty()) {
            logger.warn("Throttling detected, retrying unprocessed items...");
            try {
                Thread.sleep(1000); // Simple wait before retry
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Retry only the unprocessed items
            BatchWriteItemRequest retryRequest = BatchWriteItemRequest.builder()
                    .requestItems(response.unprocessedItems())
                    .build();
            response = ddb.batchWriteItem(retryRequest);
        }
    }
}