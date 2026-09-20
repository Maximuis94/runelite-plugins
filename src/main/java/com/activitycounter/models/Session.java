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

package com.activitycounter.models;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Skill;

/**
 * Model class for a Session, which describes a set of registered Counts within a manually defined timespan
 */
@Data
public class Session {
	private String id;

	@Getter
	@Setter
	private String sessionName;

	private long accountHash;
	private String accountName;
	private Instant startTime;
	private boolean inProgress;
	private int secondsPassed;

	private Map<Integer, Count> trackedKills;
	private Map<Skill, Integer> initialXp;
	private Map<Skill, Integer> gainedXp;

	public Session(long accountHash, String accountName) {
		id = UUID.randomUUID().toString();
		this.accountHash = accountHash;
		this.accountName = accountName;
		startTime = Instant.now();
		inProgress = true;
		secondsPassed = 0;
		trackedKills = new HashMap<>();
		initialXp = new HashMap<>();
		gainedXp = new HashMap<>();
	}

	/**
	 * Ensures maps are instantiated.
	 * Crucial for when Gson deserializes older JSON files that lack these fields.
	 */
	private void ensureXpMapsExist() {
		if (initialXp == null) {
			initialXp = new HashMap<>();
		}
		if (gainedXp == null) {
			gainedXp = new HashMap<>();
		}
	}

	public void addKillCount(Count kc) {
		if (trackedKills == null) trackedKills = new HashMap<>();
		trackedKills.put(kc.getVarPlayerId(), kc);
	}

	public Count getKillCount(int varpId) {
		if (trackedKills == null) trackedKills = new HashMap<>();
		return trackedKills.get(varpId);
	}

	public void addSecond() {
		secondsPassed++;
	}

	public boolean isTracking(int varpId) {
		if (trackedKills == null) trackedKills = new HashMap<>();
		return trackedKills.containsKey(varpId);
	}

	public Collection<Count> getAllKillCounts() {
		if (trackedKills == null) trackedKills = new HashMap<>();
		return trackedKills.values();
	}

	public void initializeSkill(Skill skill, int currentXp) {
		ensureXpMapsExist();
		if (!initialXp.containsKey(skill)) {
			initialXp.put(skill, currentXp);
			gainedXp.put(skill, 0);
		}
	}

	public boolean updateSkillXp(Skill skill, int currentXp) {
		ensureXpMapsExist();
		if (initialXp.containsKey(skill)) {
			int gained = currentXp - initialXp.get(skill);
			if (gained > gainedXp.getOrDefault(skill, 0)) {
				gainedXp.put(skill, gained);
				return true;
			}
		}
		return false;
	}

	public Map<Skill, Integer> getGainedXpMap() {
		ensureXpMapsExist();
		return gainedXp;
	}
}