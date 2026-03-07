# Data Logger
Data logger that will store various types of data locally.
Logged data is stored in subfolders located in ${user.home}/.runelite/data-logger

## Account-specific data loggers
These loggers have been written to accommodate using multiple clients simultaneously and minimize risk for I/O errors.

<details>
  <summary>Grand Exchange logger</summary>

If enabled, completed Grand Exchange offers are logged upon finalizing (i.e. immediately after cancelling or completing).
For each offer, the following datapoints are logged;
- ItemId: The OSRS item ID
- ItemName
- OfferCreationTime: Timestamp at which the offer was created
- Timestamp: Timestamp at which the offer was completed
- TradeType: BUY for a purchase, SELL for a sale
- Quantity: Quantity of items traded
- OfferQuantity: Quantity of items in the offer
- Price: Price per item traded
- OfferPrice: Price per item in the offer
- Value: Amount of GP transferred in the trade
- Tax: Total tax paid
- AccountName: Name of the account that placed the offer
- AccountHash: account-specific hash value (retained across name changes)
- GeSlot: Value of 0-7 that indicates the Grand Exchange slot used, or -1 for entries generated from the Grand Exchange history data only.
- IsHistoryEntry: If true, data is drawn solely from the Grand Exchange history UI
- IsCancelled: If true, the offer was cancelled prematurely

![img.png](images/example-grand-exchange-log.png)
_An example entry in the CSV file produced by the Grand Exchange logger_

The exchange logger is also designed to retroactively update Grand Exchange offers that were submitted upon completion,
using Grand Exchange history entry data. Updating offers this way requires opening the History interface.
A safeguard against duplicate submissions of unclaimed offers exists.

</details>

<details>
    <summary>Aggregated banked items</summary>
If enabled, the contents of banks and ongoing grand exchange offers are logged per account and also merged into a single, separate file.
Specific accounts may be excluded via plugin configurations.
Partially completed offers are added to bank data as;

- The unspent amount of GP for buy offers
- The quantity of items that is yet to be sold for sell offers

</details>

## Colosseum
Data loggers related to tracking Colosseum progress. 


<details>
    <summary>Colosseum wave logger</summary>

Logger that keeps track of Colosseum data per wave. If enabled, the following datapoints are logged;
- Wave number
- Wave status [COMPLETED/FAILED/CANCELLED/CONFIG_DISABLED]
- Account name
- Tag: User-defined tag that can be set in config menu
- Wave reward(s)
  - Dizana's quiver can also be stored as 4,000 Sunfire splinters
  - Hidden next wave loot can also be logged
- Modifiers: choices and modifier chosen
- Time taken: Wave completion time in seconds
- Damage taken: Amount of damage directly taken from enemies (i.e. that counts towards damage bonus)
- Speed/damage/modifier/completion glory earned
- Wave glory: Glory earned during this wave
- Total glory: Total glory earned so far
- Mob spawn locations: X and Y coordinates for each mob, except for Sol and Fremenniks
- Manticore sequence: Orb sequences of manticores encountered during the wave (bottom-top)

The data described above is always generated as JSON file, and may additionally also be generated as CSV file.
Logs are stored in the csv and log folder in .runelite/data-logger/colosseum

![img.png](images/example-colosseum-log-entry-csv.png)
_Example CSV row of the Colosseum wave logger, note that it does not show the entire row_
</details>

<details>
    <summary>Colosseum Timeline logger</summary>

If enabled, during every tick of each wave a game state is parsed and added to a timeline.
A state is composed of the following values;
- Wave number
- Tick number, relative to wave start, starting at 0
- Player X and Y coordinates
- NPC list. For each relevant NPC, the following data is stored;
  - NpcId
  - Name
  - X and Y coordinate
  - HP and Max HP

    Fremenniks, Solarflares, Healing totems, Bee Swarms and Beam crystals are optional and can be disabled via configurations.
Timelines are stored in .runelite/data-logger/colosseum/timeline as a JSON file, each attempt is given its own timeline file.

![img.png](images/example-colosseum-timeline-state.png)

_Example of state data at a particular tick during a particular wave_

This data could for instance be used to review previous attempts, simulate alternatives, or to observe NPC behaviour.

</details>

<details>
  <summary>Colosseum wave completion screenshots</summary>


If enabled, a screenshot is created and stored in .runelite/data-logger/screenshot/colosseum when the
interface between waves or the rewards chest interface pops up.
For each attempt, a new directory is created and all screenshots taken during that attempt are stored in that directory.
![img_1.png](images/example-wave-completion-screenshot.png)
_An example of a screenshot taken after wave 12 is completed_

</details>

