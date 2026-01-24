package dev.pgm.community.requests.store;

import dev.pgm.community.requests.RequestProfile;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface RequestStore {

  CompletableFuture<RequestProfile> login(UUID playerId);

  void update(RequestProfile profile);

  CompletableFuture<RequestProfile> query(String target);

  RequestProfile getCached(UUID playerId);
}
