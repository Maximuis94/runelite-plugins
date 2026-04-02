# Changelog

## 1.2.0
2026-03-29

### General
- Added this changelog
- Reorganized folder structure
- Added supply tracker
- Added internal Colosseum trial history file

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

### Discord webhook
- Added discord webhook service
- Added String formatting for Colosseum runs

### Screenshot logger
- Added output format to screenshots, allowing for screenshots to be saved as JPEG as well as PNG
- Added death screenshots for the ColosseumAttemptLogger

### Grand Exchange logger
- Added separate class for history entries
- Added a mechanism for submitting offers when the offers are collected en masse, provided the offer has not been submitted yet.

### Config
- Added auto merge option that will automatically merge all submitted results upon attempt completion
- Added output format option for screenshots
- Added supply tracking option for colosseum trials

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