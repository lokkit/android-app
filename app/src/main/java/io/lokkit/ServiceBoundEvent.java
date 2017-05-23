package io.lokkit;

/**
 * Created by Nick on 23.05.2017.
 */

public interface ServiceBoundEvent {
    void serviceBound();

    void serviceUnbound();
}
