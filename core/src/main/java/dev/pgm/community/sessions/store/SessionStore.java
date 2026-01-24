package dev.pgm.community.sessions.store;

import dev.pgm.community.sessions.Session;
import dev.pgm.community.sessions.SessionQuery;
import java.util.concurrent.CompletableFuture;

public interface SessionStore {

  void save(Session session);

  CompletableFuture<Session> query(SessionQuery target);

  void updateSessionEndTime(Session session);

  void endOngoingSessions();

  void endOngoingSessionsSync();
}
