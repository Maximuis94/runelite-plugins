package com.activitycounter.listeners;

import com.activitycounter.ActivityCounterConfig;
import com.activitycounter.listeners.core.ConfigListener;
import javax.inject.Inject;

/**
 * Listener class that manages a group of TrackedActivities that can be toggled via the plugin configurations.
 * As a leaf, it does not have other classes depending on it.
 */
public abstract class AbstractListenerLeaf extends AbstractListenerNode
{
	@Inject
	protected ActivityCounterConfig config;

	@Inject
	protected ConfigListener configListener;

	/**
	 * Defines the rule for whether this listener should currently be active.
	 * Typically hooked directly into a config getter.
	 */
	public abstract boolean isEnabled();

	/**
	 * Evaluates the current configuration and registers or unregisters the listener accordingly.
	 */
	public final void evaluateState() {
		if (isEnabled()) {
			register();
		} else {
			unregister();
		}
	}
}