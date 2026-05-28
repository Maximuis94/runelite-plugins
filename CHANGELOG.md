# Changelog

## 1.2.0
2026-05-28

### Summary
- Added this changelog
- Reorganized folder structure
- Added supply tracker
- Additional value tracking during Colosseum trials
- Added discord webhook
- Added dataviewers in the sidepanel
- Massively expanded itemlogger

### Supply tracker
- Added an EquipmentTracker service, which tracks consumed item charges
    - Defined various TrackedEquipment and ItemCharge instances with hard-coded parameters relevant for tracking ItemCharge consumption
    - Defined certain combat characteristics that may be used by the EquipmentTracker
    - Implemented various ways to track ItemCharge consumption
- Added a representation for supplies with multiple consumptions (ConsumableItemGroup) and logic for remapping parsed certain parsed inventory data
    - For now, it describes potions
- Added a SupplyTracker service, which tracks consumed items and item charges (the latter via EquipmentTracker)
- Added item/charge valuation to tracked supplies in the SupplyTracker and its related services
    - Potions are expressed as the summed amount of doses, which are valued using the price of the 4-dosed potion
    - Item charges are valued based on items consumed when charging the item.
- Added supply-log file generation methods

### Colosseum Attempt logger
- Extended logged trial data with supply consumption data provided by the SupplyTracker described above
- Added activeModifiers column to output, which shows all modifiers chosen so far during a wave.
- Added rewards mapping to ColosseumAttempt that merges all rewards earned during a trial
- Added supply consumption mapping to ColosseumAttempt that describes supplies/item charges consumed throughout the trial
- A separate supply log may also be generated upon finalizing a trial
- Added internal trial history file
    - Each submitted ColosseumAttempt is stored in here
    - jsonline file that may be used by to-be implemented plugin components like a data viewer
- Older trial logs can be migrated, albeit with some missing datapoints
  - New logs have a version number and will be managed via a jsonl file
  - Migrated trials are recreated and exported to the new dedicated directory (.runelite/data-logger/colosseum/trials)
- Quiver / 4k splinters are no longer explicitly logged as wave 12 reward
  - Renamed the header in the CSV file accordingly

### Discord webhook
- Added Discord webhook service for logging Colosseum trials on a Discord server
- Added various various pre-defined formats to share on a server;
  - Detailed: Shows detailed information on every wave and on the trial itself
  - Concise: A summary of the entire trial with no wave-specific information
  - Custom: A custom-defined template
  - Screenshot: A screenshot of the reward chest UI or the unfortunate moment you die can be attached to the message
  - Detailed screenshot: A screenshot with the same information as the detailed format added below the screenshot as part of the image
- Added specific conditions in which certain formats are to be shared (success/fail/claim) and thresholds like minimum reward and wave
- Added configurations that allows one to omit certain values from pre-defined templates
- Added panel to utilities that may be used to test custom webhook templates

### Item logger
- Added the following Item logger sources;
  - ItemCharge: a variety of items that have refundable charges in them can be logged
    - The logged items correspond with the refunded items, if one were to uncharge it
    - Toxic blowpipe: Scales and darts in the Toxic blowpipe are tracked
    - Serpentine helmet and toxic staff of the dead are not tracked 
    - ItemCharges are tracked using chatmessages only (update / check)
  -  Active GE offers: The part of a partially completed offer that has not yet been completed is logged
      - Buy offer: Unspent GP
      - Sell offer: Items that have not yet been sold
  - Carried items: Items that are worn or in the inventory are also tracked
  - Costume room: Items stored in the costume room are also tracked
  - Stash units: Items stored in STASH units are also tracked
- Item logs are updated whenever the information is available to the client. An update can be forced through certain interactions, like checking the noticeboard at Watson for STASH data.

### Grand Exchange logger
- Added separate class for history entries
- Added a mechanism for submitting offers when the offers are collected en masse, provided the offer has not been submitted yet.
- Added a parser for submitting offers if the collection sound effect is registered in case the offer was not registered via other routes
- Added various output formats for exported completed exchange offers
  - csv / json / jsonl
  - Bundled by day/week/month/forever for json/csv
- The GE history is now tracked separately and is not longer merged into logged exchange offers

### Config
- Added auto merge option that will automatically merge all submitted results upon attempt completion
- Added output format option for screenshots
- Added supply tracking option for colosseum trials
- Added configurations to tweak the exported JSON exchange log entries
- Added a variety of configurations for new features

### File structure
- Added a more prominent separation between internal and external files
- Internal files are logged if the associated component is enabled
  - Accessible via sidepanel
  - Used to regenerate files outside the internal directory
- External files are generated by the plugin and may be generated in various formats

### Side panel
Added a more detailed side panel with different views that may be selected via the top combobox
- Colosseum statistics
  - Contains aggregated and specific stats based on trials that have been logged
  - Has various filters one can use to include / exclude trials based on various conditions
    - Modifiers, wave numbers, tag(s), final result
    - E.g., allows one to see the modifier choices for waves in which Blasphemy I was among the active modifiers
  - Various data viewers for aggregated stats, typically per-wave;
    - Modifier choices (counts for seen & picked per modifier per wave)
    - Reward counts per wave
    - Statistics per wave number (completion time, wave glory, total glory, wave reward value)
      - For each of these stats, min, 5%, Q1, median, Q3, 95% and max values
      - Additionally, result per wave number, hitless count per wave number
- Items manager
  - Interface used to view and delete items from specific accounts and/or sources
    - Designed to clear item counts of items that one no longer has in particular
- Item viewer
  - Interface that shows aggregated item data
  - Data can be filtered by source and/or account
  - Aggregated stats are shown above the filtered subset (unique items / estimated value / N accounts / N sources)
- Utilities
  - Button panel to navigate to various directories created by the plugin
  - Manual export buttons used to manually recreate certain data using internal logs
  - Custom Colosseum template Discord webhook sandbox
    - Section in which one can write a template and immediately test it by sending it to the webhook url with a generated wave log

### Other
- Added the gameMode attribute, which describes the GameMode of the world the user has logged into (e.g. LEAGUES, REGULAR, DEADMAN, ...). 
  - This in turn prevents exchange offers and item data to be logged if logged onto a leagues / other temporary game mode world.
  - Colosseum trials have been given a gameMode that can be used to separate leagues trials from regular trials. Converted trials are always set to regular, though.
- Laid groundwork for Combat tracking for future releases
  - Combat / attack / weapon style definitions


## Previous versions

<details>
    <summary>1.1.0</summary>

## 1.1.0
2026-03-12
### Internal folder
- Cached data the plugin relies on is moved to the internal folder
- Account hash / Account name mappings are stored in a JSON file in this folder
- Active offer data / creation timestamp data of GE offers is stored
- The Grand Exchange ledger is stored here

### Colosseum tracker

#### Output files
Output files are stored in a directory specifically created for that trial.

#### Wave logging
Added logger for keeping track of data per wave. Tracked data includes completion time, glory acquired, loot, modifiers (choices&chosen), NPC spawn locations, Manticore attack sequences

#### Game state tracking
Game states that occur during the waves and are recorded every tick. A state is composed of Wave number, tick number (relative to wave start), player hp, prayer and location, NPC hp, locations and Manticore attack sequences.

#### Between-wave screenshots
Screenshots taken as the intermission UI appears, as well as after opening the rewards chest.

### Grand Exchange logger

#### Grand Exchange History parser
- Parses the 40 entries in the Grand Exchange History
- Entries are used to update previous submissions, provided they can be matched with confidence. 

#### Entry data
A Grand Exchange entry is now extended with the following datapoints;
- itemName
- tradeType [BUY/SELL]
- tax
- accountHash
- isHistoryEntry [TRUE/FALSE]
- isCancelled [TRUE/FALSE]

### Item Vault logger
Loggers for item storage data per account. As of now, it consists the following storages;
- Bank
- Seed vault
- Ongoing grand exchange offers

Each vault is defined as a list of item stacks. The Grand Exchange offers refer to the unfinished part of the offer. That is, the items received if the offer were to be cancelled. All vaults can also be merged into a single list.

### Side panel buttons
Four buttons have been added to the side panel;
- Three of them can be used to open output directories
- The fourth can be used to merge all item vault data into a single data structure
  - While doing so, account and vault type data is added to each row

</details>


<details>
    <summary>1.0.0</summary>

## 1.0.0
2026-02-25
- Framework for data logging
- Grand Exchange offer logging

</details>