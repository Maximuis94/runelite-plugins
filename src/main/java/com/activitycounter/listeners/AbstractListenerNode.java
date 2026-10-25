/*
 * Copyright (c) 2026, maximuis94 <https://github.com/maximuis94>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
	protected boolean isActive = false;

	/**
	 * Updates the activeSession variable after this session has been initialized
	 */
	public void onSessionStarted(Session session) { activeSession = session; isActive = true; }

	/**
	 * Updates the activeSession variable after this session has been terminated
	 */
	public void onSessionStopped(Session session) { activeSession = null; isActive = false; }

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