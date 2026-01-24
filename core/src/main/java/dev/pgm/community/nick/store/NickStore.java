package dev.pgm.community.nick.store;

import dev.pgm.community.nick.Nick;
import java.util.concurrent.CompletableFuture;

public interface NickStore {

  void save(Nick nick);

  CompletableFuture<Nick> query(String target);

  CompletableFuture<Boolean> update(Nick nick);

  CompletableFuture<Boolean> isNameAvailable(String name);

  CompletableFuture<Nick> queryByName(String name);
}
