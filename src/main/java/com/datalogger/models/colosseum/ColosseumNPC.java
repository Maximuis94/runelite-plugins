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

package com.datalogger.models.colosseum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A ColosseumNPC as seen in a ColosseumState. It describes an NPC at a particular place at a particular time during a
 * trial.
 * The hp attribute is an approximation based on the visible HP bar. In a nutshell, a higher maxHP value tends to translate
 * to less accurate hp value, as the visual hp bar resolution is not equal to the exact number of hp
 * Some attributes only apply to specific mobs;
 * orbSequence and isSequenceRevealed only applies to Manticores;
 * it can be completely unknown (1), retro-actively known (2), or known (3).
 * (1) orbSequence is null and isSequenceRevealed is false, OR;
 * orbSequence describes the sequence (e.g. MAGIC-RANGE-MELEE) and isSequenceRevealed is false (2) or true (3)
 * isReinforcements is used to distinguish reinforcements Serpent shamans from its non-reinforcement counterpart, as
 * all other cases are implied. By definition, it is always true for a Jaguar warrior and Minotaur.
 * One edge case that does not have a proper solution is a Red flag minotaur (npcId=12813)
 * Red flag minotaurs behave different, but this version is not (yet?) encoded in the simulator. Since these
 * minotaurs have the ability to move through other foes and cannot be safe-spotted, they are omitted completely in
 * generated replays, as they do not affect the behaviour of their allies by blocking their movement.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColosseumNPC {
	private int npcIndex;
	private int npcId;
	private String name;
	private int x;
	private int y;
	private int hp;
	private int maxHp;

	private String orbSequence;
	@Builder.Default
	private boolean isSequenceRevealed = false;

	@Builder.Default
	private boolean isReinforcements = false;
}