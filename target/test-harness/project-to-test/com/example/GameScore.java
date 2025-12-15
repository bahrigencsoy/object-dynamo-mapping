package com.example;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.lib.*;

public class GameScore implements Comparable<GameScore> {

    private final String userId;
    private final String gameTitle;
    private final String gameGenre;
    private final Integer totalScore;

    private transient DynamoDbClient _client;
    private transient String _tableName;

    GameScore(Builder b) {
        this.userId = b.userId;
        this.gameTitle = b.gameTitle;
        this.gameGenre = b.gameGenre;
        this.totalScore = b.totalScore;
    }

    public String userId() {
        return userId;
    }

    public String gameTitle() {
        return gameTitle;
    }

    public String gameGenre() {
        return gameGenre;
    }

    public Integer totalScore() {
        return totalScore;
    }

    void _setClient(DynamoDbClient client) {
        this._client = client;
    }

    void _setTableName(String tableName) {
        this._tableName = Objects.requireNonNull(tableName);
    }

    public Mutator mutator() {
        var map = new HashMap<String, AttributeValue>();
        GameScore._user_id__helper.contributeToMap(map, userId);

        GameScore._game_title__helper.contributeToMap(map, gameTitle);;
        return new Mutator(_client, map, _tableName);
    }

    public static class Mutator {

        private final List<FieldMutator> mutators = new ArrayList<>();
        private final DynamoDbClient client;
        private final String tableName;
        private final Map<String, AttributeValue> key;

        Mutator(DynamoDbClient client, Map<String, AttributeValue> key, String tableName) {
            this.client = client;
            this.key = Map.copyOf(key);
            this.tableName = Objects.requireNonNull(tableName);
        }

        public GenericMutator<String, Mutator> userId() {
            var mutator = new GenericMutator<String, Mutator>("user_id", this, _user_id__helper);
            mutators.add(mutator);
            return mutator;
        }
        public GenericMutator<String, Mutator> gameTitle() {
            var mutator = new GenericMutator<String, Mutator>("game_title", this, _game_title__helper);
            mutators.add(mutator);
            return mutator;
        }
        public GenericMutator<String, Mutator> gameGenre() {
            var mutator = new GenericMutator<String, Mutator>("game_genre", this, _game_genre__helper);
            mutators.add(mutator);
            return mutator;
        }
        public GenericMutator<Integer, Mutator> totalScore() {
            var mutator = new GenericMutator<Integer, Mutator>("total_score", this, _total_score__helper);
            mutators.add(mutator);
            return mutator;
        }

        public GameScore commit() {
            AtomicInteger counter = new AtomicInteger();
            List<String> setExpressions = new ArrayList<>();
            List<String> removeExpressions = new ArrayList<>();
            List<String> conditionExpressions = new ArrayList<>();
            Map<String, String> expressionAttributeNames = new HashMap<>();
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            for (var mutator : mutators) {
                mutator.appendUpdateExpression(counter, setExpressions, removeExpressions, conditionExpressions,
                        expressionAttributeNames, expressionAttributeValues);
            }
            StringBuilder updateExpression = new StringBuilder();
            if (!setExpressions.isEmpty()) {
                updateExpression.append("SET ");
                updateExpression.append(String.join(",", setExpressions));
                updateExpression.append(" ");
            }
            if (!removeExpressions.isEmpty()) {
                updateExpression.append("REMOVE ");
                updateExpression.append(String.join(",", removeExpressions));
                updateExpression.append(" ");
            }
            var updateItemRequestBuilder = UpdateItemRequest.builder().key(key).tableName(tableName)
                    .updateExpression(updateExpression.toString()).expressionAttributeNames(expressionAttributeNames)
                    .returnValues(ReturnValue.ALL_NEW);
            if (!expressionAttributeValues.isEmpty()) {
                updateItemRequestBuilder.expressionAttributeValues(expressionAttributeValues);
            }
            if (!conditionExpressions.isEmpty()) {
                updateItemRequestBuilder.conditionExpression(String.join(" AND ", conditionExpressions));
            }
            UpdateItemResponse response = client.updateItem(updateItemRequestBuilder.build());
            Map<String, AttributeValue> map = response.attributes();
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
            obj._setClient(client);
            obj._setTableName(tableName);
            return obj;
        }

        public void delete() {
            DeleteItemRequest request = DeleteItemRequest.builder().tableName(tableName).key(key).build();
            var response = client.deleteItem(request);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        if (o == this)
            return true;
        GameScore other = (GameScore) o;
        return Objects.equals(userId, other.userId) && Objects.equals(gameTitle, other.gameTitle)
                && Objects.equals(gameGenre, other.gameGenre) && Objects.equals(totalScore, other.totalScore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, gameTitle, gameGenre, totalScore);
    }

    @Override
    public int compareTo(GameScore o) {
        int c;

        if (this.userId == null && o.userId == null)
            return 0;
        if (this.userId == null)
            return 1;
        if (o.userId == null)
            return -1;
        if ((c = this.userId.compareTo(o.userId)) != 0) {
            return c;
        }
        if (this.gameTitle == null && o.gameTitle == null)
            return 0;
        if (this.gameTitle == null)
            return 1;
        if (o.gameTitle == null)
            return -1;
        if ((c = this.gameTitle.compareTo(o.gameTitle)) != 0) {
            return c;
        }
        if (this.gameGenre == null && o.gameGenre == null)
            return 0;
        if (this.gameGenre == null)
            return 1;
        if (o.gameGenre == null)
            return -1;
        if ((c = this.gameGenre.compareTo(o.gameGenre)) != 0) {
            return c;
        }

        return 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GameScore{");

        sb.append("userId='").append(userId).append('\'');
        sb.append(", gameTitle='").append(gameTitle).append('\'');
        sb.append(", gameGenre='").append(gameGenre).append('\'');
        sb.append(", totalScore='").append(totalScore).append('\'');

        sb.append('}');
        return sb.toString();
    }

    static final lib.AttributeHelper<String> _user_id__helper = new lib.StringAttributeHelper("user_id");
    static final lib.AttributeHelper<String> _game_title__helper = new lib.StringAttributeHelper("game_title");
    static final lib.AttributeHelper<String> _game_genre__helper = new lib.StringAttributeHelper("game_genre");
    static final lib.AttributeHelper<Integer> _total_score__helper = new lib.IntegerAttributeHelper("total_score");

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String userId;
        private String gameTitle;
        private String gameGenre;
        private Integer totalScore;

        private Builder() {
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        public Builder gameTitle(String gameTitle) {
            this.gameTitle = gameTitle;
            return this;
        }
        public Builder gameGenre(String gameGenre) {
            this.gameGenre = gameGenre;
            return this;
        }
        public Builder totalScore(Integer totalScore) {
            this.totalScore = totalScore;
            return this;
        }

        public GameScore build() {
            return new GameScore(this);
        }
    }

}