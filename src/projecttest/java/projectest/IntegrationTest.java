package projectest;

import com.example.CacheResource;
import com.example.GameScore;
import com.example.GameScoreEntityManager;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ALL")
public class IntegrationTest {

    private DynamoDbClient client;
    private GameScoreEntityManager em;
    private GameScore score;

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
        test.step_300_query_by_global_secondary_index();
        test.step_301_query_by_local_secondary_index();
    }

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
        score = em.putGameScore("_" + random.nextLong(), "__" + random.nextLong(), null, random.nextInt(), false);
    }

    void step_102_delete_1_item() {
        score.mutator().delete();
    }

    void step_103_query_1_item() {
        assertFalse(em.findGameScore(score.userId(), score.gameTitle()).isPresent());
        assertFalse(em.queryGameScoreByUserId("dava").totalScore().eq(10).execute().findFirst().isPresent());
    }

    void step_104_insert_and_query_1_item_again() {
        var newScore = em.putGameScore(score.userId(), score.gameTitle(), score.gameGenre(), -1, false);
        assertNotEquals(score, newScore);
        score = newScore;
        newScore = em.findGameScore(score.userId(), score.gameTitle()).get();
        assertEquals(score, newScore);
        newScore = em.queryGameScoreByUserId(score.userId()).execute().findFirst().get();
        assertEquals(score, newScore);
        newScore.mutator().delete();
    }

    void step_105_more_detailed_queries() {
        assertThrows(DynamoDbException.class, () -> em.queryGameScoreByUserId(null).execute());
        em.putGameScore("aaa", "shooter blaster", "shooter", 100, false);
        em.putGameScore("aaa", "space traveler", "shooter", 200, false);
        assertEquals(2, em.queryGameScoreByUserId("aaa").execute().count());
        assertThrows(DynamoDbException.class, () -> em.queryGameScoreByUserId("bbb").userId().ne("xxx").execute());
    }

    void step_106_update_item() {
        score = em.putGameScore("user", "my game", null, 100, false);
        score = em.findGameScore("user", "my game").get();
        score.mutator().totalScore().setValue(200).commit();
        score = em.findGameScore("user", "my game").get();
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
        var cache = em.putCacheResource("a", new byte[]{1, 2, 3}, null, Map.of("a", "x", "b", "y"), false);
        cache = em.findCacheResource("a").get();
        assertArrayEquals(new byte[]{1, 2, 3}, cache.data());
        assertEquals(Map.of("b", "y", "a", "x"), cache.properties());
        cache.mutator().delete();
    }

    void step_201_raced_atomic_puts() {
        int threadCount = 10;
        long startTime = System.currentTimeMillis();
        Phaser phaser = new Phaser(threadCount + 1);
        AtomicInteger replacedCount = new AtomicInteger(0);
        AtomicInteger racedCount = new AtomicInteger(0);
        for (int i = 0; i < threadCount; i++) {
            final int totalScore = i;
            new Thread(() -> {
                phaser.arriveAndAwaitAdvance();
                try {
                    GameScore previous = em.putGameScore("user", "game", null, totalScore, true);
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
                    em.putGameScore("user", "game " + rand.nextInt(), "x" + rand.nextInt(3), rand.nextInt(), false);
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

    void step_300_query_by_global_secondary_index() {
        assertEquals(0, em.scanAllCacheResource().count());
        em.putCacheResource("key", new byte[]{1, 2, 3}, "uniq1", Map.of(), true);
        assertEquals(1, em.queryCacheResourceByUniqueId("uniq1").execute().count());
        CacheResource resource = em.queryCacheResourceByUniqueId("uniq1").execute().findFirst().get();
        assertArrayEquals(new byte[]{1, 2, 3}, resource.data());
        em.putCacheResource("key2", new byte[]{4, 5, 6}, "uniq1", Map.of(), true);
        em.putCacheResource("key3", new byte[]{7, 8, 9}, "uniq2", Map.of(), true);
        assertEquals(2, em.queryCacheResourceByUniqueId("uniq1").execute().count());
        assertThrows(DynamoDbException.class, () -> em.queryCacheResourceByUniqueId("uniq1").uniqueId().ne("uniq1").execute());
        assertArrayEquals(new byte[]{7, 8, 9}, em.queryCacheResourceByKey("key3").uniqueId().ne("x").execute().findFirst().get().data());
    }

    void step_301_query_by_local_secondary_index() {
        assertEquals(0, em.queryGameScoreByGameGenre("a").gameGenre().eq("b").execute().count());
        assertEquals(0, em.queryGameScoreByGameGenre("a").gameGenre().beginsWith("b").execute().count());

        em.putGameScore("user1", "space invaders", "shooter", 100, false);
        em.putGameScore("user2", "space invaders", "shooter", 200, false);
        em.putGameScore("user1", "moon blaster", "adventure", 300, false);
        em.putGameScore("user2", "moon blaster", "adventure", 400, false);
        em.putGameScore("user2", "catcher", "platform", 500, false);

        assertEquals(3, em.queryGameScoreByGameGenre("user2").execute().count());
        assertEquals(1, em.queryGameScoreByGameGenre("user2").gameGenre().beginsWith("adv").execute().count());
    }

}
