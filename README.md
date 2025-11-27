Building with a self smoke test:

```shell
mvn clean test && mvn test-compile -Pproject-test && mvn clean install -DskipTests
```

### Usage

[See the code](./src/projecttest/java/projectest/Usage.java)
```java
package projectest;

import com.example.GameScore;
import com.example.GameScoreEntityManager;

public class Usage {

    GameScoreEntityManager em;

    void initializeEntityManager() {
        // You need to supply valid DynamoDbClient instance
        em = new GameScoreEntityManager(null /* replace */);
    }

    void insertEntity(){

        GameScore score = em.putGameScore("user_10", "Another World", null);
    }

    void queryAndUpdateEntity() {
        GameScore score = em.queryGameScore("user_10", "Another World").get();

        score.mutator()
                .totalScore().setValue(123L)
                .commit();
    }
}
```

[See the input json](./src/test/resources/project-to-test/model.json)
```json
{
  "version": "0.2",
  "packageName": "com.example",
  "factoryClass": "GameScoreEntityManager",
  "tables": [
    {
      "name": "games_scores_odm_test",
      "partitionKey": {
        "name": "user_id",
        "type": "STRING",
        "attribute": "userId"
      },
      "sortKey": {
        "name": "game_title",
        "type": "STRING",
        "attribute": "gameTitle"
      },
      "items": [
        {
          "name": "GameScore",
          "attributes": [
            {
              "name": "total_score",
              "type": "NUMBER",
              "attribute": "totalScore"
            }
          ]
        }
      ]
    }
  ]
}
```

[See the sample Maven pom](./src/projecttest/java/projectest/pom.xml)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>my-dynamo-app</artifactId>
    <version>0.1</version>

    <properties>
        <jdk.version>21</jdk.version>
        <release.version>21</release.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>dynamodb</artifactId>
            <version>2.39.3</version>
            <exclusions>
                <exclusion>
                    <groupId>software.amazon.awssdk</groupId>
                    <artifactId>netty-nio-client</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>software.amazon.awssdk</groupId>
                    <artifactId>apache-client</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>net.gencsoy.odm</groupId>
                <artifactId>odm-maven-plugin</artifactId>
                <version>0.2</version>
                <executions>
                    <execution>
                        <id>generate-dynamo-files</id>
                        <goals>
                            <goal>touch</goal>
                        </goals>
                        <configuration>
                            <inputModel>${project.basedir}/src/main/resources/dynamo-model.json</inputModel>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>build-helper-maven-plugin</artifactId>
                <version>3.6.1</version>
                <executions>
                    <execution>
                        <id>add-generated-odm-sources</id>
                        <goals>
                            <goal>add-source</goal>
                        </goals>
                        <configuration>
                            <sources>${project.basedir}/target/generated-sources/odm</sources>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```