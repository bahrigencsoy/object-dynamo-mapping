package projectest;

import com.example.GameScore;
import com.example.GameScoreEntityManager;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ALL")
public class IntegrationTest {

    private DynamoDbClient client;
    private GameScoreEntityManager em;
    private GameScore score;

    void step_001_create_client() {
        client = DynamoDbClient.builder()
                .httpClientBuilder(ApacheHttpClient.builder())
                .region(Region.US_EAST_1)
                .build();
    }

    void step_002_delete_all_items() {
        DynamoDbTruncateTool.truncateTable(client, "game_scores_odm_test");
        DynamoDbTruncateTool.truncateTable(client, "cached_resource_odm_test");
    }

    void step_003_create_em() {
        em = new GameScoreEntityManager(client);
    }

    void step_101_insert_1_item() {
        Random random = new Random();
        score = em.putGameScore("_" + random.nextLong(), "__" + random.nextLong(), random.nextInt());
    }

    void step_102_delete_1_item() {
        score.mutator().delete();
    }

    void step_103_query_1_item() {
        assertFalse(em.queryGameScore(score.getUserId(), score.getGameTitle()).isPresent());
    }

    void step_104_insert_and_query_1_item_again() {
        var newScore = em.putGameScore(score.getUserId(), score.getGameTitle(), -1);
        assertEquals(score.getUserId(), newScore.getUserId());
        assertEquals(score.getGameTitle(), newScore.getGameTitle());
        assertEquals(-1, newScore.getTotalScore());
        newScore = em.queryGameScore(score.getUserId(), score.getGameTitle()).get();
        assertEquals(score.getUserId(), newScore.getUserId());
        assertEquals(score.getGameTitle(), newScore.getGameTitle());
        assertEquals(-1, newScore.getTotalScore());
        newScore.mutator().delete();
    }

    void step_105_update_item() {
        score = em.putGameScore("user", "my game", 100);
        score = em.queryGameScore("user", "my game").get();
        score.mutator().totalScore().setValue(200).commit();
        score = em.queryGameScore("user", "my game").get();
        assertEquals(200, score.getTotalScore());
    }

    void step_101_cache_item_tests() {
        var cache = em.putCacheResource("a", new byte[]{1, 2, 3});
        cache = em.queryCacheResource("a").get();
        assertArrayEquals(new byte[]{1, 2, 3}, cache.getData());
    }

    void step_310_insert_20_items() {
        for (int i = 0; i < 10; i++) {
            em.putGameScore("user_" + i, "shooter blaster", Math.abs(i * 10));
        }
        for (int i = 5; i < 15; i++) {
            em.putGameScore("user_" + i, "monster hunter", Math.abs(i * 20));
        }
    }

    public static void main(String[] args) {
        IntegrationTest test = new IntegrationTest();
        test.step_001_create_client();
        test.step_002_delete_all_items();
        test.step_003_create_em();
        test.step_101_insert_1_item();
        test.step_102_delete_1_item();
        test.step_103_query_1_item();
        test.step_104_insert_and_query_1_item_again();
        test.step_105_update_item();
        ;
        test.step_310_insert_20_items();
    }


}
