package dev.pgm.community.sessions.services;

public interface SessionDataQuery {

  static final String TABLE_NAME = "sessions";
  static final String TABLE_FIELDS =
      "(id VARCHAR(36) PRIMARY KEY, player VARCHAR(36), disguised BOOL, server VARCHAR(32), start_time BIGINT, end_time BIGINT)";

  static final String INSERT_SESSION_QUERY = "INSERT INTO " + TABLE_NAME
      + "(id, player, disguised, server, start_time, end_time) VALUES (?, ?, ?, ?, ?, ?)";

  static final String UPDATE_SESSION_ENDTIME_QUERY =
      "UPDATE " + TABLE_NAME + " SET end_time = ? WHERE id = ?";

  static final String UPDATE_ONGOING_SESSION_ENDING_QUERY =
      "UPDATE " + TABLE_NAME + " SET end_time = ? WHERE server = ? AND end_time IS NULL";

  static final String LATEST_TABLE_NAME = "latest_sessions";
  static final String LATEST_TABLE_FIELDS =
      "(player VARCHAR(36), ignore_disguised BOOL, session_id VARCHAR(36), disguised BOOL, server VARCHAR(32), start_time BIGINT, end_time BIGINT, PRIMARY KEY (player, ignore_disguised))";

  static final String SELECT_LATEST_SESSION_QUERY =
      "SELECT * FROM " + LATEST_TABLE_NAME + " WHERE player = ? AND ignore_disguised = ? LIMIT 1";

  static final String UPDATE_LATEST_ENDTIME_QUERY =
      "UPDATE " + LATEST_TABLE_NAME + " SET end_time = ? WHERE session_id = ?";

  static final String UPDATE_LATEST_ONGOING_SESSION_ENDING_QUERY =
      "UPDATE " + LATEST_TABLE_NAME + " SET end_time = ? WHERE server = ? AND end_time IS NULL";
}
