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

import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TrackedActivityTest
{
	@Test
	public void testAllActivitiesHaveRequiredIds()
	{
		for (TrackedActivity activity : TrackedActivity.values())
		{
			TrackerType type = activity.getTrackerType();
			if (type == TrackerType.VARBIT_VALUE ||
				type == TrackerType.VARPLAYER_VALUE ||
				type == TrackerType.SOUND_EFFECT ||
				type == TrackerType.AREA_SOUND)
			{
				assertNotNull(
					activity.name() + " is marked as " + type + " but has no gameSourceIds",
					activity.getGameSourceIds()
				);
				assertTrue(
					activity.name() + " has an empty gameSourceIds array",
					activity.getGameSourceIds().length > 0
				);
			}
		}
	}

	@Test
	public void testStorageIdsAreUniqueAndMapped()
	{
		Set<Integer> seenIds = new HashSet<>();
		for (TrackedActivity activity : TrackedActivity.values())
		{
			int id = activity.getId();
			assertTrue("Duplicate storage ID detected: " + id + " on " + activity.name(), seenIds.add(id));
			assertSame("STORAGE_ID_MAP returned incorrect instance for ID " + id,
				activity, TrackedActivity.getByStorageId(id));
		}
	}
}