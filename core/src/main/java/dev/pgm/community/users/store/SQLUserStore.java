package dev.pgm.community.users.store;

import dev.pgm.community.users.UserProfile;
import dev.pgm.community.users.services.AddressHistoryService;
import dev.pgm.community.users.services.AddressHistoryService.LatestAddressInfo;
import dev.pgm.community.users.services.SQLUserService;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLUserStore implements UserStore {

  private final SQLUserService userService;
  private final AddressHistoryService addressService;

  public SQLUserStore() {
    this.userService = new SQLUserService();
    this.addressService = new AddressHistoryService();
  }

  @Override
  public CompletableFuture<UserProfile> getProfile(String query) {
    return userService.query(query);
  }

  @Override
  public CompletableFuture<UserProfile> getProfile(UUID id) {
    return userService.query(id.toString());
  }

  @Override
  public CompletableFuture<UserProfile> login(UUID id, String username, String address) {
    return userService.login(id, username, address);
  }

  @Override
  public CompletableFuture<Set<String>> getKnownIps(UUID playerId) {
    return addressService.getKnownIps(playerId);
  }

  @Override
  public CompletableFuture<Set<UUID>> getAlternateAccounts(UUID playerId) {
    return addressService.getAlternateAccounts(playerId);
  }

  @Override
  public CompletableFuture<LatestAddressInfo> getLatestAddress(UUID playerId) {
    return addressService.getLatestAddressInfo(playerId);
  }

  @Override
  public void trackIp(UUID id, String address) {
    addressService.trackIp(id, address);
  }

  @Override
  public void save(UserProfile profile) {
    userService.save(profile);
  }

  @Override
  public CompletableFuture<Integer> count() {
    return userService.count();
  }
}
