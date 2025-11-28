package projectest;

import com.example.GameScore;
import com.example.GameScoreEntityManager;

public class Usage {

    GameScoreEntityManager em;

    void initializeEntityManager() {
        // You need to supply valid DynamoDbClient instance
        em = new GameScoreEntityManager(null /* replace */);
    }

    void insertEntity() {

        GameScore score = em.putGameScore("user_10", "Another World", "Adventure", 100, false);
    }

    void queryAndUpdateEntity() {
        GameScore score = em.queryGameScore("user_10", "Another World").get();

        score.mutator()
                .totalScore().setValue(123)
                .commit();
    }
}
