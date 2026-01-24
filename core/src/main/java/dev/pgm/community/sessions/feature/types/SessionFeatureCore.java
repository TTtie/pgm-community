package dev.pgm.community.sessions.feature.types;

import dev.pgm.community.Community;
import dev.pgm.community.feature.FeatureBase;
import dev.pgm.community.sessions.Session;
import dev.pgm.community.sessions.SessionQuery;
import dev.pgm.community.sessions.VanishedSessionListener;
import dev.pgm.community.sessions.feature.SessionFeature;
import dev.pgm.community.sessions.store.SessionStore;
import dev.pgm.community.users.feature.UsersFeature;
import dev.pgm.community.utils.PGMUtils;
import dev.pgm.community.utils.VisibilityUtils;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import tc.oc.pgm.events.CountdownStartEvent;
import tc.oc.pgm.restart.RestartCountdown;

public class SessionFeatureCore extends FeatureBase implements SessionFeature {

  private final SessionStore store;
  private List<UUID> joiningPlayers;
  private VanishedSessionListener vanishedSessionListener;
  private boolean serverRestarting;

  public SessionFeatureCore(UsersFeature users, Logger logger, SessionStore store) {
    super(users.getConfig(), logger, "Sessions");
    this.store = store;

    if (getConfig().isEnabled()) {
      this.joiningPlayers = new ArrayList<>();
      enable();

      if (PGMUtils.isPGMEnabled()) {
        vanishedSessionListener = new VanishedSessionListener(this);
        Bukkit.getPluginManager().registerEvents(vanishedSessionListener, Community.get());
      }
    }
  }

  @Override
  public void disable() {
    if (vanishedSessionListener != null) HandlerList.unregisterAll(vanishedSessionListener);
    endOngoingSessionsSync();
  }

  @Override
  public CompletableFuture<Session> getLatestSession(UUID playerId, boolean ignoreDisguised) {
    return store.query(new SessionQuery(playerId, ignoreDisguised));
  }

  @Override
  public Session startSession(Player player) {
    Session session = new Session(player.getUniqueId(), VisibilityUtils.isDisguised(player));
    store.save(session);

    return session;
  }

  @Override
  public void endSession(Session session) {
    session.setEndDate(Instant.now());
    store.updateSessionEndTime(session);
  }

  @Override
  public void endOngoingSessions() {
    store.endOngoingSessions();
  }

  @Override
  public void endOngoingSessionsSync() {
    store.endOngoingSessionsSync();
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onJoinLowest(PlayerJoinEvent event) {
    joiningPlayers.add(event.getPlayer().getUniqueId());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onJoinHighest(PlayerJoinEvent event) {
    if (!serverRestarting) startSession(event.getPlayer());
    joiningPlayers.remove(event.getPlayer().getUniqueId());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onQuitHighest(PlayerQuitEvent event) {
    if (!serverRestarting)
      getLatestSession(event.getPlayer().getUniqueId(), false).thenAcceptAsync(this::endSession);
  }

  @Override
  public boolean isPlayerJoining(Player player) {
    return joiningPlayers.contains(player.getUniqueId());
  }

  @EventHandler
  public void onServerRestart(CountdownStartEvent event) {
    if (!(event.getCountdown() instanceof RestartCountdown)) return;

    // When restarting, end sessions early to avoid flooding queries.
    serverRestarting = true;
    endOngoingSessions();
  }
}
