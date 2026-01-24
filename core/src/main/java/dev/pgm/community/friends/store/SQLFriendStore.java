package dev.pgm.community.friends.store;

import dev.pgm.community.friends.Friendship;
import dev.pgm.community.friends.services.SQLFriendshipService;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SQLFriendStore implements FriendStore {

  private final SQLFriendshipService service;

  public SQLFriendStore() {
    this.service = new SQLFriendshipService();
  }

  @Override
  public void save(Friendship friendship) {
    service.save(friendship);
  }

  @Override
  public void updateFriendshipStatus(Friendship friendship, boolean accept) {
    service.updateFriendshipStatus(friendship, accept);
  }

  @Override
  public CompletableFuture<List<Friendship>> queryList(String target) {
    return service.queryList(target);
  }

  @Override
  public CompletableFuture<Integer> count() {
    return service.count();
  }
}
