package dev.pgm.community.requests.store;

import dev.pgm.community.requests.RequestProfile;
import dev.pgm.community.requests.services.SQLRequestService;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLRequestStore implements RequestStore {

  private final SQLRequestService service;

  public SQLRequestStore() {
    this.service = new SQLRequestService();
  }

  @Override
  public CompletableFuture<RequestProfile> login(UUID playerId) {
    return service.login(playerId);
  }

  @Override
  public void update(RequestProfile profile) {
    service.update(profile);
  }

  @Override
  public CompletableFuture<RequestProfile> query(String target) {
    return service.query(target);
  }

  @Override
  public RequestProfile getCached(UUID playerId) {
    return service.getCached(playerId);
  }
}
