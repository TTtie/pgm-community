package dev.pgm.community.database.dialect;

public class SqliteDialect implements SqlDialect {

  @Override
  public String upsertLatestSessionQuery() {
    return "INSERT INTO latest_sessions"
        + "(player, ignore_disguised, session_id, disguised, server, start_time, end_time)"
        + " VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT(player, ignore_disguised) DO UPDATE SET "
        + "session_id = excluded.session_id, disguised = excluded.disguised, "
        + "server = excluded.server, start_time = excluded.start_time, end_time = excluded.end_time";
  }
}
