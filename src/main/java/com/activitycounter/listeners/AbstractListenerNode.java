package com.activitycounter.listeners;

import com.activitycounter.ActivityCounterPlugin;
import com.activitycounter.models.Session;
import javax.inject.Inject;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.client.eventbus.EventBus;

/**
 * Abstract class that provides basic functionality for a Listener class that needs to be (un)registered by the Plugin
 * class, as well as the means to be able to detect whether it should (not) do things.
 * Unregistered classes should not do things, unless it's directly related to any of the methods defined in this
 * framework. All extensions of this class that are not extensions of the conditional listener class may assume that
 * they are active and registered while the plugin is active.
 */
public abstract class AbstractListenerNode
{
	@Inject
	private EventBus eventBus;

	@Inject
	protected ActivityCounterPlugin plugin;

	@Inject
	protected Client client;

	@Getter
	private boolean isRegistered = false;

	protected Session activeSession;

	/**
	 * Updates the activeSession variable after this session has been initialized
	 */
	public void onSessionStarted(Session session)
	{
		this.activeSession = session;
	}

	/**
	 * Updates the activeSession variable after this session has been terminated
	 */
	public void onSessionStopped(Session session)
	{
		this.activeSession = null;
	}

	/**
	 * Registers the listener with the EventBus.
	 */
	public final void register() {
		if (!isRegistered) {
			eventBus.register(this);
			isRegistered = true;
			onRegistered();
		}
	}

	/**
	 * Unregisters the listener from the EventBus.
	 */
	public final void unregister() {
		if (isRegistered) {
			eventBus.unregister(this);
			isRegistered = false;
			onUnregistered();
		}
	}

	/**
	 * Optional hook for subclasses to perform setup immediately after registration.
	 */
	protected void onRegistered() {}

	/**
	 * Optional hook for subclasses to perform cleanup immediately after unregistration.
	 */
	protected void onUnregistered() {}
}