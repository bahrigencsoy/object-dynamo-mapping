package projectest;

import com.example.CacheResource;
import com.example.GameScore;
import com.example.GameScoreEntityManager;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

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
        test.step_108_conditional_update();
        test.step_111_cache_item_tests();
        test.step_201_raced_atomic_puts();
        test.step_300_query_by_global_secondary_index();
        test.step_301_query_by_local_secondary_index();
        test.step_310_raced_multimap();
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
        em = new GameScoreEntityManager(client, "cached_resource_odm_test");
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
        em.findGameScore("aaa", "space traveler").get().mutator().totalScore().delete().commit();
        assertNotNull(em.findGameScore("aaa", "space traveler").get().gameGenre());
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

    void step_108_conditional_update() {
        GameScore gs = em.putGameScore("cond", "cond-title", "adventure", 100, false);
        gs = gs.mutator().totalScore().setValue(101).commit();
        assertThrows(ConditionalCheckFailedException.class, () -> em.findGameScore("cond", "cond-title").get().mutator().totalScore().setValue(200).totalScore().condition(100).commit());
        assertEquals(101, em.findGameScore("cond", "cond-title").get().totalScore());
        gs.mutator()
                .totalScore().increment(99)
                .totalScore().condition(101)
                .gameGenre().condition("adventure")
                .gameGenre().setValue("shooter")
                .commit();
        assertEquals(200, em.findGameScore("cond", "cond-title").get().totalScore());
        assertEquals("shooter", em.findGameScore("cond", "cond-title").get().gameGenre());
        em.findGameScore("cond", "cond-title").get().mutator().delete();
    }

    void step_111_cache_item_tests() {
        var cache = em.putCacheResource("a", new byte[]{1, 2, 3}, null, Map.of("a", "x", "b", "y"), null, Instant.now(), false);
        cache = em.findCacheResource("a").get();
        assertArrayEquals(new byte[]{1, 2, 3}, cache.data());
        assertEquals(Map.of("b", "y", "a", "x"), cache.properties());
        cache.mutator().properties().put("b", "yy").commit();
        cache = em.findCacheResource("a").get();
        assertEquals("yy", cache.properties().get("b"));
        cache.mutator().delete();
        cache = em.putCacheResource(CacheResource.builder().key("xyz").uniqueId("jjj").build(), false);
        // You cannot update a non initialized map
        assertThrows(DynamoDbException.class, () -> em.findCacheResource("xyz").get().mutator().properties().put("aa", "bb").commit());
        cache = em.putCacheResource(CacheResource.builder().key("xyz").properties(Map.of()).build(), false);
        cache = em.findCacheResource("xyz").get();
        assertNull(cache.uniqueId());
        cache.mutator().properties().put("klm", "qrs").commit();
        assertEquals("qrs", em.findCacheResource("xyz").get().properties().get("klm"));
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
                phaser.arriveAndAwaitAdvance(); // PHASE 0 - start at once
                try {
                    // Try to atomically add an entity, only one will succeed
                    GameScore previous = em.putGameScore("user", "game", null, totalScore, true);
                    if (previous.totalScore().equals(totalScore)) {
                        replacedCount.incrementAndGet();
                    } else {
                        racedCount.incrementAndGet();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                phaser.arriveAndAwaitAdvance(); // PHASE 1 - end of atomic put test

                GameScore previous = em.findGameScore("user", "game").get();
                phaser.arriveAndAwaitAdvance(); // PHASE 2 - start of atomic update test
                // Increment the game score by one
                previous.mutator().totalScore().increment(1).commit();
                previous.mutator().totalScore().increment(5).commit();
                previous.mutator().totalScore().increment(-2).commit();

                phaser.arriveAndAwaitAdvance(); // PHASE 3 - end of atomic update test
                for (int j = 0; j < 5; j++) {
                    Random rand = new Random();
                    em.putGameScore("user", "game " + rand.nextInt(), "x" + rand.nextInt(3), rand.nextInt(), false);
                }

                phaser.arriveAndAwaitAdvance(); // PHASE 4 - end of test
            }).start();
        }
        phaser.arriveAndAwaitAdvance(); // PHASE 0 - start at once

        phaser.arriveAndAwaitAdvance(); // PHASE 1 - end of atomic put test
        System.err.format("Took %d msecs to insert with %d threads%n", System.currentTimeMillis() - startTime, threadCount);
        assertEquals(1, replacedCount.get());
        assertEquals(threadCount - 1, racedCount.get());

        em.findGameScore("user", "game").get().mutator().totalScore().setValue(100).commit();
        phaser.arriveAndAwaitAdvance(); // PHASE 2 - start of atomic update test
        phaser.arriveAndAwaitAdvance(); // PHASE 3 - end of atomic update test
        assertEquals(100 + threadCount * 4, em.findGameScore("user", "game").get().totalScore());

        phaser.arriveAndAwaitAdvance(); // PHASE 4 - end of test
        System.err.format("Insertions finished%n");
        assertEquals(1 + threadCount * 5, em.scanAllGameScore().count());
    }

    void step_300_query_by_global_secondary_index() {
        assertEquals(0, em.scanAllCacheResource().count());
        em.putCacheResource("key", new byte[]{1, 2, 3}, "uniq1", Map.of(), Map.of(), Instant.now(), true);
        assertEquals(1, em.queryCacheResourceByUniqueId("uniq1").execute().count());
        CacheResource resource = em.queryCacheResourceByUniqueId("uniq1").execute().findFirst().get();
        assertArrayEquals(new byte[]{1, 2, 3}, resource.data());
        em.putCacheResource("key2", new byte[]{4, 5, 6}, "uniq1", Map.of(), Map.of(), Instant.now(), true);
        em.putCacheResource("key3", new byte[]{7, 8, 9}, "uniq2", Map.of(), Map.of(), null, true);
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

    void step_310_raced_multimap() {
        int threadCount = 16;
        Phaser phaser = new Phaser(threadCount + 1);
        for (int i = 0; i < threadCount; i++) {
            final String threadId = "thread" + i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    phaser.arriveAndAwaitAdvance(); // Before put main item
                    CacheResource resource = em.putCacheResource(CacheResource.builder().key("xxxyyy").extendedProperties(Map.of()).build(), true);
                    phaser.arriveAndAwaitAdvance(); // Before race
                    resource.mutator().extendedProperties().add("t", threadId).commit();
                    phaser.arriveAndAwaitAdvance(); // After race
                }
            }).start();
        }
        Map<String, List<String>> extended = new HashMap<>();
        extended.put("a", List.of("1", "2", "3"));
        extended.put("b", List.of("4", "5"));
        em.putCacheResource(CacheResource.builder().key("aaabbb").extendedProperties(extended).build(), false);
        assertEquals(List.of("4", "5", "7"), em.findCacheResource("aaabbb").get().mutator().extendedProperties().add("b", "7").commit().extendedProperties().get("b"));
        phaser.arriveAndAwaitAdvance(); // Before put main item
        phaser.arriveAndAwaitAdvance(); // Before race
        phaser.arriveAndAwaitAdvance(); // After race
        assertEquals(threadCount, em.findCacheResource("xxxyyy").get().extendedProperties().get("t").size());// After race
        List<String> list = em.findCacheResource("xxxyyy").get().extendedProperties().get("t");
        IntStream.range(0, threadCount).forEach(i -> assertTrue(list.contains("thread" + i), "" + i + " => " + list));
    }

}
