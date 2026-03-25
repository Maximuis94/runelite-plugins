# Changelog
## 1.0.0
2026-02-25
- Framework for data logging
- Grand Exchange offer logging

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

## 1.1.1
### General
- Added this changelog
- Reorganized folder structure

### Config
- Added auto merge option that will automatically merge all submitted results upon attempt completion
- Added output format option for screenshots
- Added supply tracking option for colosseum trials

### Colosseum Attempt logger
- Added a supply tracker to the colosseum attempt logger, which tracks consumed supplies during a trial and Tumeken's shadow / Scythe of vitur charges
- Added logic for merging potion doses in the supply tracker
- Added time tracking of boss wave phase transitions
- Added activeModifiers column to output, which shows all modifiers chosen so far during a wave.

### Screenshot logger
- Added output format to screenshots, allowing for screenshots to be saved as JPEG as well as PNG

### Grand Exchange logger
- Added separate class for history entries
- Added a mechanism for submitting offers when the offers are collected en masse, provided the offer has not been submitted yet.