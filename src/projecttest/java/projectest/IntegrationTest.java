package projectest;

import com.example.GameScore;
import com.example.GameScoreEntityManager;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.util.Random;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

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
        score = em.putGameScore("_" + random.nextLong(), "__" + random.nextLong(), random.nextInt(), false);
    }

    void step_102_delete_1_item() {
        score.mutator().delete();
    }

    void step_103_query_1_item() {
        assertFalse(em.queryGameScore(score.userId(), score.gameTitle()).isPresent());
        assertFalse(em.queryGameScore().userId().eq("dava").totalScore().eq(10).execute().findFirst().isPresent());
    }

    void step_104_insert_and_query_1_item_again() {
        var newScore = em.putGameScore(score.userId(), score.gameTitle(), -1, false);
        assertNotEquals(score, newScore);
        score = newScore;
        newScore = em.queryGameScore(score.userId(), score.gameTitle()).get();
        assertEquals(score, newScore);
        newScore = em.queryGameScore().userId().eq(score.userId()).execute().findFirst().get();
        assertEquals(score, newScore);
        newScore.mutator().delete();
    }

    void step_105_more_detailed_queries() {
        assertThrows(DynamoDbException.class, () -> em.queryGameScore().execute());
        em.putGameScore("aaa", "shooter blaster", 100, false);
        em.putGameScore("aaa", "space traveler", 200, false);
        assertEquals(2, em.queryGameScore().userId().eq("aaa").execute().count());
        assertThrows(DynamoDbException.class, () -> em.queryGameScore().userId().ne("bbb").execute());
    }

    void step_106_update_item() {
        score = em.putGameScore("user", "my game", 100, false);
        score = em.queryGameScore("user", "my game").get();
        score.mutator().totalScore().setValue(200).commit();
        score = em.queryGameScore("user", "my game").get();
        assertEquals(200, score.totalScore());
    }

    void step_107_scan_items() {
        AtomicInteger count = new AtomicInteger();
        em.scanAllGameScore().forEach(gs -> {
            count.incrementAndGet();
            gs.mutator().delete();
        });
        assertEquals(3, count.get());
    }

    void step_111_cache_item_tests() {
        var cache = em.putCacheResource("a", null, new byte[]{1, 2, 3}, false);
        cache = em.queryCacheResource("a").get();
        assertArrayEquals(new byte[]{1, 2, 3}, cache.data());
        cache.mutator().delete();
    }

    void step_201_raced_atomic_puts() {
        int threadCount = 50;
        long startTime = System.currentTimeMillis();
        Phaser phaser = new Phaser(threadCount + 1);
        AtomicInteger replacedCount = new AtomicInteger(0);
        AtomicInteger racedCount = new AtomicInteger(0);
        for (int i = 0; i < threadCount; i++) {
            final int totalScore = i;
            new Thread(() -> {
                phaser.arriveAndAwaitAdvance();
                try {
                    GameScore previous = em.putGameScore("user", "game", totalScore, true);
                    if (previous.totalScore().equals(totalScore)) {
                        replacedCount.incrementAndGet();
                    } else {
                        racedCount.incrementAndGet();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                phaser.arriveAndAwaitAdvance();
                for (int j = 0; j < 5; j++) {
                    Random rand = new Random();
                    em.putGameScore("user", "game " + rand.nextInt(), rand.nextInt(), false);
                }
                phaser.arriveAndAwaitAdvance();
            }).start();
        }
        phaser.arriveAndAwaitAdvance();
        phaser.arriveAndAwaitAdvance();
        System.err.format("Took %d msecs to insert with %d threads%n", System.currentTimeMillis() - startTime, threadCount);
        assertEquals(1, replacedCount.get());
        assertEquals(threadCount - 1, racedCount.get());
        phaser.arriveAndAwaitAdvance();
        System.err.format("Insertions finished%n");
        assertEquals(1 + threadCount * 5, em.scanAllGameScore().count());
    }

    void step_310_insert_20_items() {
        for (int i = 0; i < 10; i++) {
            em.putGameScore("user_" + i, "shooter blaster", Math.abs(i * 10), false);
        }
        for (int i = 5; i < 15; i++) {
            em.putGameScore("user_" + i, "monster hunter", Math.abs(i * 20), false);
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
        test.step_105_more_detailed_queries();
        test.step_106_update_item();
        test.step_107_scan_items();
        test.step_111_cache_item_tests();
        test.step_201_raced_atomic_puts();
        test.step_310_insert_20_items();
    }

}
