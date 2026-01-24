package dev.pgm.community.users.store;

import dev.pgm.community.users.UserProfile;
import dev.pgm.community.users.services.AddressHistoryService.LatestAddressInfo;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserStore {

  CompletableFuture<UserProfile> getProfile(String query);

  CompletableFuture<UserProfile> getProfile(UUID id);

  CompletableFuture<UserProfile> login(UUID id, String username, String address);

  CompletableFuture<Set<String>> getKnownIps(UUID playerId);

  CompletableFuture<Set<UUID>> getAlternateAccounts(UUID playerId);

  CompletableFuture<LatestAddressInfo> getLatestAddress(UUID playerId);

  void trackIp(UUID id, String address);

  void save(UserProfile profile);

  CompletableFuture<Integer> count();
}
