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
```

[See the input json](./src/test/resources/project-to-test/model.json)
```json
{
  "version": "0.3",
  "packageName": "com.example",
  "factoryClass": "GameScoreEntityManager",
  "tables": [
    {
      "name": "game_scores_odm_test",
      "javaClass": "GameScore",
      "primaryKey": {
        "partitionKey": "user_id",
        "sortKey": "gameTitle"
      },
      "attributes": [
        {
          "name": "user_id",
          "type": "STRING",
          "attribute": "userId"
        },
        {
          "name": "game_title",
          "type": "STRING",
          "attribute": "gameTitle"
        },
        {
          "name": "total_score",
          "type": "NUMBER",
          "attribute": "totalScore",
          "javaType": "Integer"
        }
      ],
      "indexes": [
        {
          "name": "game_genres_idx",
          "sortKey": "game_genre",
          "_comment": "^^ no partition key means that this is a local secondary index"
        }
      ],
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
      "localSecondaryIndexes": {
        "game_genres_idx": {
          "name": "game_genre",
          "type": "STRING",
          "attribute": "gameGenre"
        }
      },
      "items": [
        {
          "name": "GameScore",
          "attributes": [
            {
              "name": "total_score",
              "type": "NUMBER",
              "attribute": "totalScore",
              "javaType": "Integer"
            }
          ]
        }
      ]
    },
    {
      "name": "cached_resource_odm_test",
      "partitionKey": {
        "name": "cache_item_key",
        "type": "STRING",
        "attribute": "key"
      },
      "globalSecondaryIndexes": {
        "cache_id_idx": {
          "name": "cached_item_unique_id",
          "type": "STRING",
          "attribute": "uniqueId"
        }
      },
      "items": [
        {
          "name": "CacheResource",
          "attributes": [
            {
              "name": "cached_data",
              "type": "BINARY",
              "attribute": "data"
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
    <version>1.2.3</version>

    <properties>
        <jdk.version>17</jdk.version>
        <release.version>17</release.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>dynamodb-enhanced</artifactId>
            <version>2.39.4</version>
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
                <version>0.3-SNAPSHOT</version>
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