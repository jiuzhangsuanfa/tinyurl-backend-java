package com.jiuzhang.url.dbconfig;

import com.jiuzhang.url.enums.DBShard;

import java.util.Objects;

public class DBShardContext {
    private static final ThreadLocal<DBShard> dbShardHolder = new ThreadLocal<>();

    public static DBShard getDBShard() {
        DBShard dbShard = dbShardHolder.get();
        return Objects.isNull(dbShard) ? DBShard.url0 : dbShard;
    }

    /*
    public static void setDBShard(DBShard tenant) {
        dbShardHolder.set(tenant);
    }

    public static void clearDBShard() {
        dbShardHolder.remove();
    }
    */
}
