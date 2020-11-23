package com.jiuzhang.url.dbconfig;

import com.jiuzhang.url.enums.DBShard;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;

public class FlywayMigrationInitializer {

    @Autowired
    private RoutingDataSource routingDataSource;

    public void migrate() {
        String scriptLocation = "db/migration";

        for (DBShard shard : DBShard.values()) {
            String dbName = shard.name();

            Flyway flyway = Flyway.configure()
                    .locations(scriptLocation)
                    .baselineOnMigrate(Boolean.TRUE)
                    .dataSource(routingDataSource.getDataSourceByShard(shard))
                    .schemas(dbName)
                    .load();

            flyway.migrate();
        }
    }
}
