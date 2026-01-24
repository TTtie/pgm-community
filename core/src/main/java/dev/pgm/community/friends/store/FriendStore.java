package dev.pgm.community.friends.store;

import dev.pgm.community.friends.Friendship;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FriendStore {

  void save(Friendship friendship);

  void updateFriendshipStatus(Friendship friendship, boolean accept);

  CompletableFuture<List<Friendship>> queryList(String target);

  CompletableFuture<Integer> count();
}
