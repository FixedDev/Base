package us.sparknetwork.base.user.session;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.UUID;

public interface SessionHandler {
    ListenableFuture<Session> getSession(UUID id);

    ListenableFuture<Session> createSession(UUID id);

    ListenableFuture<Void> deleteSession(UUID id);

    ListenableFuture<Boolean> isOnline(UUID id);
}
