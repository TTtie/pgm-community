package dev.pgm.community.assistance.store;

import dev.pgm.community.assistance.Report;
import dev.pgm.community.assistance.services.SQLAssistanceService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLAssistanceStore implements AssistanceStore {

  private final SQLAssistanceService service;

  public SQLAssistanceStore() {
    this.service = new SQLAssistanceService();
  }

  @Override
  public void save(Report report) {
    service.save(report);
  }

  @Override
  public CompletableFuture<List<Report>> queryList(String target) {
    return service.queryList(target);
  }

  @Override
  public CompletableFuture<Integer> count() {
    return service.count();
  }

  @Override
  public void invalidate(UUID playerId) {
    service.invalidate(playerId);
  }
}
