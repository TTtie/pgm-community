package dev.pgm.community.nick.store;

import dev.pgm.community.nick.Nick;
import dev.pgm.community.nick.NickConfig;
import dev.pgm.community.nick.services.SQLNickService;
import java.util.concurrent.CompletableFuture;

public class SQLNickStore implements NickStore {

  private final SQLNickService service;

  public SQLNickStore(NickConfig config) {
    this.service = new SQLNickService(config);
  }

  @Override
  public void save(Nick nick) {
    service.save(nick);
  }

  @Override
  public CompletableFuture<Nick> query(String target) {
    return service.query(target);
  }

  @Override
  public CompletableFuture<Boolean> update(Nick nick) {
    return service.update(nick);
  }

  @Override
  public CompletableFuture<Boolean> isNameAvailable(String name) {
    return service.isNameAvailable(name);
  }

  @Override
  public CompletableFuture<Nick> queryByName(String name) {
    return service.queryByName(name);
  }
}
