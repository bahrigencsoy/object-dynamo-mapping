package projectest;

import com.example.GameScore;
import com.example.GameScoreEntityManager;

public class Usage {

    void testCompile(){
        GameScoreEntityManager em = new GameScoreEntityManager(null);

        GameScore score = em.putGameScore("aa", "bbb", null);

        score.mutator().totalScore().setValue("111");
    }
}
