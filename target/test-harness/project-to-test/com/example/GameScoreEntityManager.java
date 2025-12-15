package com.example;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.StreamSupport;
import java.util.function.Function;

import static com.example.lib.*;

public class GameScoreEntityManager {

    private final DynamoDbClient client;

    private final String table_GameScore;
    private final String table_CacheResource;

    public GameScoreEntityManager(DynamoDbClient client, String table_CacheResource) {
        this.client = client;

        this.table_CacheResource = table_CacheResource.intern();

        this.table_GameScore = "game_scores_odm_test";

    }

    private static GameScore __constructGameScore(Map<String, AttributeValue> map) {
        GameScore.Builder builder = GameScore.builder();

        if (map.containsKey("user_id")) {
            builder.userId(GameScore._user_id__helper.extractFromMap(map));
        }
        if (map.containsKey("game_title")) {
            builder.gameTitle(GameScore._game_title__helper.extractFromMap(map));
        }
        if (map.containsKey("game_genre")) {
            builder.gameGenre(GameScore._game_genre__helper.extractFromMap(map));
        }
        if (map.containsKey("total_score")) {
            builder.totalScore(GameScore._total_score__helper.extractFromMap(map));
        }

        var obj = builder.build();
        return obj;
    }

    /**
      * Atomically puts an item or overwrite it.
      */
    public GameScore putGameScore(String userId, String gameTitle, String gameGenre, Integer totalScore,
            boolean atomic) {
        if (userId == null) {
            throw new NullPointerException("Partition key \"user_id\" is null");
        }

        if (gameTitle == null) {
            throw new NullPointerException("Sort key \"game_title\" is null");
        }

        Map<String, AttributeValue> map = new HashMap<>();
        GameScore._user_id__helper.contributeToMap(map, userId);

        GameScore._game_title__helper.contributeToMap(map, gameTitle);

        if (userId != null) {
            GameScore._user_id__helper.contributeToMap(map, userId);
        }
        if (gameTitle != null) {
            GameScore._game_title__helper.contributeToMap(map, gameTitle);
        }
        if (gameGenre != null) {
            GameScore._game_genre__helper.contributeToMap(map, gameGenre);
        }
        if (totalScore != null) {
            GameScore._total_score__helper.contributeToMap(map, totalScore);
        }

        var builder = PutItemRequest.builder().tableName(table_GameScore).item(map);
        if (atomic) {
            builder.conditionExpression("attribute_not_exists(user_id) AND attribute_not_exists(game_title)");
            builder.returnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD);
        } else {
            builder.returnValues(ReturnValue.ALL_OLD);
        }

        try {
            var response = client.putItem(builder.build());
            if (response.hasAttributes() && !atomic) {
                map.putAll(response.attributes());
            }
            var obj = __constructGameScore(map);
            obj._setClient(client);
            obj._setTableName(table_GameScore);
            return obj;
        } catch (ConditionalCheckFailedException e) {
            if (e.hasItem() && atomic) {
                var previousObject = __constructGameScore(e.item());
                previousObject._setClient(client);
                previousObject._setTableName(table_GameScore);
                return previousObject;
            } else {
                throw e;
            }
        }
    }

    public GameScore putGameScore(GameScore object, boolean atomic) {
        return putGameScore(

                object.userId(), object.gameTitle(), object.gameGenre(), object.totalScore(),

                atomic);
    }

    public Optional<GameScore> findGameScore(String userId, String gameTitle) {
        Map<String, AttributeValue> map = new HashMap<String, AttributeValue>();
        GameScore._user_id__helper.contributeToMap(map, userId);

        GameScore._game_title__helper.contributeToMap(map, gameTitle);

        GetItemRequest getItemRequest = GetItemRequest.builder().tableName(table_GameScore).key(map).build();

        GetItemResponse result = client.getItem(getItemRequest);

        if (result.item().isEmpty()) {
            return Optional.empty();
        }
        map = result.item();
        var obj = __constructGameScore(map);
        obj._setClient(client);
        obj._setTableName(table_GameScore);
        return Optional.of(obj);
    }

    public abstract class GameScoreQuery {
        private final QueryRequest.Builder builder;
        protected final Map<String, Condition> keyConditions;
        protected final Map<String, Condition> queryFilter;

        private GameScoreQuery() {
            this.builder = QueryRequest.builder();
            this.keyConditions = new LinkedHashMap<>();
            this.queryFilter = new LinkedHashMap<>();
        }

        private void _setTableName(String tableName) {
            this.builder.tableName(tableName);
        }

        private void _setIndexName(String indexName) {
            this.builder.indexName(indexName);
        }

        protected abstract Map<String, Condition> selectMapForAttribute(String attribute);

        public lib.GenericQuery<String, GameScoreQuery> userId() {
            return new lib.GenericQuery<>("user_id", this, GameScore._user_id__helper,
                    selectMapForAttribute("user_id"));
        }
        public lib.GenericQuery<String, GameScoreQuery> gameTitle() {
            return new lib.GenericQuery<>("game_title", this, GameScore._game_title__helper,
                    selectMapForAttribute("game_title"));
        }
        public lib.GenericQuery<String, GameScoreQuery> gameGenre() {
            return new lib.GenericQuery<>("game_genre", this, GameScore._game_genre__helper,
                    selectMapForAttribute("game_genre"));
        }
        public lib.GenericQuery<Integer, GameScoreQuery> totalScore() {
            return new lib.GenericQuery<>("total_score", this, GameScore._total_score__helper,
                    selectMapForAttribute("total_score"));
        }

        public java.util.stream.Stream<GameScore> execute() {
            if (!keyConditions.isEmpty()) {
                builder.keyConditions(keyConditions);
            }
            if (!queryFilter.isEmpty()) {
                builder.queryFilter(queryFilter);
            }
            var iterator = executeGameScoreQuery(builder.build());
            var split = Spliterators.spliteratorUnknownSize(iterator,
                    Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL);
            return StreamSupport.stream(split, false);
        }
    }

    public GameScoreQuery queryGameScoreByUserId(String userId) {
        GameScoreQuery query = new GameScoreQuery() {

            @Override
            protected Map<String, Condition> selectMapForAttribute(String attribute) {
                return switch (attribute) {

                    case "user_id" -> keyConditions;
                    case "game_title" -> keyConditions;

                    case "game_genre" -> queryFilter;
                    case "total_score" -> queryFilter;

                    default -> throw new IllegalArgumentException("Unknown attribute '" + attribute + "'");
                };
            }
        };
        query._setTableName(table_GameScore);

        query.userId().eq(userId);

        return query;
    }
    public GameScoreQuery queryGameScoreByGameGenre(String userId) {
        GameScoreQuery query = new GameScoreQuery() {

            @Override
            protected Map<String, Condition> selectMapForAttribute(String attribute) {
                return switch (attribute) {

                    case "user_id" -> keyConditions;
                    case "game_genre" -> keyConditions;

                    case "game_title" -> queryFilter;
                    case "total_score" -> queryFilter;

                    default -> throw new IllegalArgumentException("Unknown attribute '" + attribute + "'");
                };
            }
        };
        query._setTableName(table_GameScore);

        query._setIndexName("game_genres_idx");

        query.userId().eq(userId);

        return query;
    }

    public java.util.stream.Stream<GameScore> scanAllGameScore() {
        var iterator = executeGameScoreScan(ScanRequest.builder().tableName(table_GameScore).build());
        var split = Spliterators.spliteratorUnknownSize(iterator,
                Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL);
        return StreamSupport.stream(split, false);
    }

    private Iterator<GameScore> executeGameScoreQuery(QueryRequest queryRequest) {
        AtomicReference<QueryResponse> currentResponse = new AtomicReference<>(client.query(queryRequest));
        AtomicReference<Iterator<Map<String, AttributeValue>>> currentIterator = new AtomicReference<>(
                currentResponse.get().items().iterator());
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                if (currentIterator.get().hasNext()) {
                    return true;
                } else {
                    if (currentResponse.get().hasLastEvaluatedKey()) {
                        Map<String, AttributeValue> lastEvaluatedKey = currentResponse.get().lastEvaluatedKey();
                        QueryRequest copy = queryRequest.copy(builder -> builder.exclusiveStartKey(lastEvaluatedKey));
                        currentResponse.set(client.query(copy));
                        currentIterator.set(currentResponse.get().items().iterator());
                        return true;
                    } else {
                        return false;
                    }
                }
            }

            @Override
            public GameScore next() {
                Map<String, AttributeValue> map = currentIterator.get().next();
                var obj = __constructGameScore(map);
                obj._setClient(client);
                obj._setTableName(table_GameScore);
                return obj;
            }
        };
    }

    private Iterator<GameScore> executeGameScoreScan(ScanRequest scanRequest) {
        AtomicReference<ScanResponse> currentResponse = new AtomicReference<>(client.scan(scanRequest));
        AtomicReference<Iterator<Map<String, AttributeValue>>> currentIterator = new AtomicReference<>(
                currentResponse.get().items().iterator());
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                if (currentIterator.get().hasNext()) {
                    return true;
                } else {
                    if (currentResponse.get().hasLastEvaluatedKey()) {
                        Map<String, AttributeValue> lastEvaluatedKey = currentResponse.get().lastEvaluatedKey();
                        ScanRequest copy = scanRequest.copy(builder -> builder.exclusiveStartKey(lastEvaluatedKey));
                        currentResponse.set(client.scan(copy));
                        currentIterator.set(currentResponse.get().items().iterator());
                        return true;
                    } else {
                        return false;
                    }
                }
            }

            @Override
            public GameScore next() {
                Map<String, AttributeValue> map = currentIterator.get().next();
                var obj = __constructGameScore(map);
                obj._setClient(client);
                obj._setTableName(table_GameScore);
                return obj;
            }
        };
    }

    private static CacheResource __constructCacheResource(Map<String, AttributeValue> map) {
        CacheResource.Builder builder = CacheResource.builder();

        if (map.containsKey("cache_item_key")) {
            builder.key(CacheResource._cache_item_key__helper.extractFromMap(map));
        }
        if (map.containsKey("cached_data")) {
            builder.data(CacheResource._cached_data__helper.extractFromMap(map));
        }
        if (map.containsKey("cached_item_unique_id")) {
            builder.uniqueId(CacheResource._cached_item_unique_id__helper.extractFromMap(map));
        }
        if (map.containsKey("cache_data_props")) {
            builder.properties(CacheResource._cache_data_props__helper.extractFromMap(map));
        }
        if (map.containsKey("cache_data_extended_props")) {
            builder.extendedProperties(CacheResource._cache_data_extended_props__helper.extractFromMap(map));
        }
        if (map.containsKey("creation_time")) {
            builder.creationTime(CacheResource._creation_time__helper.extractFromMap(map));
        }

        var obj = builder.build();
        return obj;
    }

    /**
      * Atomically puts an item or overwrite it.
      */
    public CacheResource putCacheResource(String key, byte[] data, String uniqueId, Map<String, String> properties,
            Map<String, List<String>> extendedProperties, java.time.Instant creationTime, boolean atomic) {
        if (key == null) {
            throw new NullPointerException("Partition key \"cache_item_key\" is null");
        }

        Map<String, AttributeValue> map = new HashMap<>();
        CacheResource._cache_item_key__helper.contributeToMap(map, key);

        if (key != null) {
            CacheResource._cache_item_key__helper.contributeToMap(map, key);
        }
        if (data != null) {
            CacheResource._cached_data__helper.contributeToMap(map, data);
        }
        if (uniqueId != null) {
            CacheResource._cached_item_unique_id__helper.contributeToMap(map, uniqueId);
        }
        if (properties != null) {
            CacheResource._cache_data_props__helper.contributeToMap(map, properties);
        }
        if (extendedProperties != null) {
            CacheResource._cache_data_extended_props__helper.contributeToMap(map, extendedProperties);
        }
        if (creationTime != null) {
            CacheResource._creation_time__helper.contributeToMap(map, creationTime);
        }

        var builder = PutItemRequest.builder().tableName(table_CacheResource).item(map);
        if (atomic) {
            builder.conditionExpression("attribute_not_exists(cache_item_key)");
            builder.returnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD);
        } else {
            builder.returnValues(ReturnValue.ALL_OLD);
        }

        try {
            var response = client.putItem(builder.build());
            if (response.hasAttributes() && !atomic) {
                map.putAll(response.attributes());
            }
            var obj = __constructCacheResource(map);
            obj._setClient(client);
            obj._setTableName(table_CacheResource);
            return obj;
        } catch (ConditionalCheckFailedException e) {
            if (e.hasItem() && atomic) {
                var previousObject = __constructCacheResource(e.item());
                previousObject._setClient(client);
                previousObject._setTableName(table_CacheResource);
                return previousObject;
            } else {
                throw e;
            }
        }
    }

    public CacheResource putCacheResource(CacheResource object, boolean atomic) {
        return putCacheResource(

                object.key(), object.data(), object.uniqueId(), object.properties(), object.extendedProperties(),
                object.creationTime(),

                atomic);
    }

    public Optional<CacheResource> findCacheResource(String key) {
        Map<String, AttributeValue> map = new HashMap<String, AttributeValue>();
        CacheResource._cache_item_key__helper.contributeToMap(map, key);

        GetItemRequest getItemRequest = GetItemRequest.builder().tableName(table_CacheResource).key(map).build();

        GetItemResponse result = client.getItem(getItemRequest);

        if (result.item().isEmpty()) {
            return Optional.empty();
        }
        map = result.item();
        var obj = __constructCacheResource(map);
        obj._setClient(client);
        obj._setTableName(table_CacheResource);
        return Optional.of(obj);
    }

    public abstract class CacheResourceQuery {
        private final QueryRequest.Builder builder;
        protected final Map<String, Condition> keyConditions;
        protected final Map<String, Condition> queryFilter;

        private CacheResourceQuery() {
            this.builder = QueryRequest.builder();
            this.keyConditions = new LinkedHashMap<>();
            this.queryFilter = new LinkedHashMap<>();
        }

        private void _setTableName(String tableName) {
            this.builder.tableName(tableName);
        }

        private void _setIndexName(String indexName) {
            this.builder.indexName(indexName);
        }

        protected abstract Map<String, Condition> selectMapForAttribute(String attribute);

        public lib.GenericQuery<String, CacheResourceQuery> key() {
            return new lib.GenericQuery<>("cache_item_key", this, CacheResource._cache_item_key__helper,
                    selectMapForAttribute("cache_item_key"));
        }
        public lib.GenericQuery<byte[], CacheResourceQuery> data() {
            return new lib.GenericQuery<>("cached_data", this, CacheResource._cached_data__helper,
                    selectMapForAttribute("cached_data"));
        }
        public lib.GenericQuery<String, CacheResourceQuery> uniqueId() {
            return new lib.GenericQuery<>("cached_item_unique_id", this, CacheResource._cached_item_unique_id__helper,
                    selectMapForAttribute("cached_item_unique_id"));
        }
        public lib.GenericQuery<Map<String, String>, CacheResourceQuery> properties() {
            return new lib.GenericQuery<>("cache_data_props", this, CacheResource._cache_data_props__helper,
                    selectMapForAttribute("cache_data_props"));
        }
        public lib.GenericQuery<Map<String, List<String>>, CacheResourceQuery> extendedProperties() {
            return new lib.GenericQuery<>("cache_data_extended_props", this,
                    CacheResource._cache_data_extended_props__helper,
                    selectMapForAttribute("cache_data_extended_props"));
        }
        public lib.GenericQuery<java.time.Instant, CacheResourceQuery> creationTime() {
            return new lib.GenericQuery<>("creation_time", this, CacheResource._creation_time__helper,
                    selectMapForAttribute("creation_time"));
        }

        public java.util.stream.Stream<CacheResource> execute() {
            if (!keyConditions.isEmpty()) {
                builder.keyConditions(keyConditions);
            }
            if (!queryFilter.isEmpty()) {
                builder.queryFilter(queryFilter);
            }
            var iterator = executeCacheResourceQuery(builder.build());
            var split = Spliterators.spliteratorUnknownSize(iterator,
                    Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL);
            return StreamSupport.stream(split, false);
        }
    }

    public CacheResourceQuery queryCacheResourceByKey(String key) {
        CacheResourceQuery query = new CacheResourceQuery() {

            @Override
            protected Map<String, Condition> selectMapForAttribute(String attribute) {
                return switch (attribute) {

                    case "cache_item_key" -> keyConditions;

                    case "cached_data" -> queryFilter;
                    case "cached_item_unique_id" -> queryFilter;
                    case "cache_data_props" -> queryFilter;
                    case "cache_data_extended_props" -> queryFilter;
                    case "creation_time" -> queryFilter;

                    default -> throw new IllegalArgumentException("Unknown attribute '" + attribute + "'");
                };
            }
        };
        query._setTableName(table_CacheResource);

        query.key().eq(key);

        return query;
    }
    public CacheResourceQuery queryCacheResourceByUniqueId(String uniqueId) {
        CacheResourceQuery query = new CacheResourceQuery() {

            @Override
            protected Map<String, Condition> selectMapForAttribute(String attribute) {
                return switch (attribute) {

                    case "cached_item_unique_id" -> keyConditions;

                    case "cache_item_key" -> queryFilter;
                    case "cached_data" -> queryFilter;
                    case "cache_data_props" -> queryFilter;
                    case "cache_data_extended_props" -> queryFilter;
                    case "creation_time" -> queryFilter;

                    default -> throw new IllegalArgumentException("Unknown attribute '" + attribute + "'");
                };
            }
        };
        query._setTableName(table_CacheResource);

        query._setIndexName("cache_id_idx");

        query.uniqueId().eq(uniqueId);

        return query;
    }

    public java.util.stream.Stream<CacheResource> scanAllCacheResource() {
        var iterator = executeCacheResourceScan(ScanRequest.builder().tableName(table_CacheResource).build());
        var split = Spliterators.spliteratorUnknownSize(iterator,
                Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL);
        return StreamSupport.stream(split, false);
    }

    private Iterator<CacheResource> executeCacheResourceQuery(QueryRequest queryRequest) {
        AtomicReference<QueryResponse> currentResponse = new AtomicReference<>(client.query(queryRequest));
        AtomicReference<Iterator<Map<String, AttributeValue>>> currentIterator = new AtomicReference<>(
                currentResponse.get().items().iterator());
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                if (currentIterator.get().hasNext()) {
                    return true;
                } else {
                    if (currentResponse.get().hasLastEvaluatedKey()) {
                        Map<String, AttributeValue> lastEvaluatedKey = currentResponse.get().lastEvaluatedKey();
                        QueryRequest copy = queryRequest.copy(builder -> builder.exclusiveStartKey(lastEvaluatedKey));
                        currentResponse.set(client.query(copy));
                        currentIterator.set(currentResponse.get().items().iterator());
                        return true;
                    } else {
                        return false;
                    }
                }
            }

            @Override
            public CacheResource next() {
                Map<String, AttributeValue> map = currentIterator.get().next();
                var obj = __constructCacheResource(map);
                obj._setClient(client);
                obj._setTableName(table_CacheResource);
                return obj;
            }
        };
    }

    private Iterator<CacheResource> executeCacheResourceScan(ScanRequest scanRequest) {
        AtomicReference<ScanResponse> currentResponse = new AtomicReference<>(client.scan(scanRequest));
        AtomicReference<Iterator<Map<String, AttributeValue>>> currentIterator = new AtomicReference<>(
                currentResponse.get().items().iterator());
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                if (currentIterator.get().hasNext()) {
                    return true;
                } else {
                    if (currentResponse.get().hasLastEvaluatedKey()) {
                        Map<String, AttributeValue> lastEvaluatedKey = currentResponse.get().lastEvaluatedKey();
                        ScanRequest copy = scanRequest.copy(builder -> builder.exclusiveStartKey(lastEvaluatedKey));
                        currentResponse.set(client.scan(copy));
                        currentIterator.set(currentResponse.get().items().iterator());
                        return true;
                    } else {
                        return false;
                    }
                }
            }

            @Override
            public CacheResource next() {
                Map<String, AttributeValue> map = currentIterator.get().next();
                var obj = __constructCacheResource(map);
                obj._setClient(client);
                obj._setTableName(table_CacheResource);
                return obj;
            }
        };
    }

}