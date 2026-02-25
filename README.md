# Data Logger
Data logger that will store various types of data locally.
Logged data is stored folders located in ${user.home}/.runelite/data-logger

## Grand Exchange Logger
If enabled, completed Grand Exchange offers are submitted upon completion. 
For each offer, the following datapoints are submitted; 
- item_id: OSRS item ID
- timestamp: Timestamp on which the offer was completed
- timestamp created: Timestamp on which the offer was created
- is buy: true if the offer was a purchase (TRUE/FALSE)
- quantity: The quantity of items that were traded
- offer quantity: The quantity on the offer
- price: The average price per traded item
- offer price: The price on the offer
- value: Total amount of GP transferred
- account name: Name of the account that submitted the exchange offer
- grand exchange slot index: Index of the Grand Exchange slot (0-7)
