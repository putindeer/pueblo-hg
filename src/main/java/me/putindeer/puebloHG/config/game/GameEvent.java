package me.putindeer.puebloHG.config.game;

/**
 * @param triggerTime en segundos desde el inicio
 */
public record GameEvent(GameEventType type, int triggerTime, Runnable action) {
}