# Dynamic line of sight
## Player line of sight
The following lines of sight may be drawn around the player;
- Attack range of the current weapon and style
- Attack range of manual spell casts (attack range of 10)
- Up to 5 customizable attack ranges. E.g., can be used to pre-define various attack ranges of weapon switches. 

Each line of sight can be drawn with its own outline and fill color.


During Fortis Colosseum trials, the active Myopia tier will be factored into all attack ranges with the exception of the manual spell cast. <br>
Furthermore, a keybind may be defined to allow the player line of sight to be toggled on/off by pressing this keybind. 

### Virtual player line of sight
If the virtual line of sight keybind is configured, the player lines of sight will be drawn as if the player is standing at the location of the cursor while the button is pressed (see video)

## NPC line of sight
For foes encountered in the TzHaar Fight Caves, the Inferno, TzHaar-Ket-Rak's challenges and Fortis Colosseum, the NPC line of sight can be drawn as well.<br>
If this option is enabled, hovering over an NPC will display its line of sight. The color corresponds with combat style and may be configured per group.<br>
Additionally, a keybind can be set to make the line of sight only appear while the keybind is pressed. If this is not defined, the lines of sight will always be drawn while hovering over an NPC.

## Examples
![debug-los-myopia2-example.png](images/debug-los-myopia2-example.png)
_Example of various pre-defined lines of sight, with Myopia II active_

![javelin-colossus-los-example.PNG](images/javelin-colossus-los-example.png)</br>
_Example of the NPC line of sight of a Javelin colossus behind a pillar_

<video src="https://github.com/user-attachments/assets/dc9edb6f-4a21-46b2-8370-55847c3208a0" controls="controls" muted="muted" loop="loop" width="600"> </video>

_Example of the line of sights drawn from both the player and the cursor with Myopia II active_

![corner-trap-example.png](images/corner-trap-example.png)
_Example of how the virtual player Line of Sight could be used to identify corner traps at a distance_

![debug-minimus-npc-los-15-range-example.png](images/debug-minimus-npc-los-15-range-example.png)
_Fictitious example of the NPC line of sight, if Minimus were to have an attack range of 15_