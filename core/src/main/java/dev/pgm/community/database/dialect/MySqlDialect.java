package dev.pgm.community.database.dialect;

public class MySqlDialect implements SqlDialect {

  @Override
  public String upsertLatestSessionQuery() {
    return "INSERT INTO latest_sessions"
        + "(player, ignore_disguised, session_id, disguised, server, start_time, end_time)"
        + " VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
        + "session_id = VALUES(session_id), disguised = VALUES(disguised), "
        + "server = VALUES(server), start_time = VALUES(start_time), end_time = VALUES(end_time)";
  }
}
