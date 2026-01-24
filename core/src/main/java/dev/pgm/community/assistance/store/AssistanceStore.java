package dev.pgm.community.assistance.store;

import dev.pgm.community.assistance.Report;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AssistanceStore {

  void save(Report report);

  CompletableFuture<List<Report>> queryList(String target);

  CompletableFuture<Integer> count();

  void invalidate(UUID playerId);
}
