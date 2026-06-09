# Game Design Principles

This file captures high-level gameplay principles that guide feature work and balancing.

This is an homage to a 90s shareware game called Pirate's Plunder created in 1995 by a now-defunct company called Dexterity Software.

## Objective
- You are **Captain Nevarro** of the ship **Odyssey**, searching the rich waters of the **Andorian Sea** for ancient sunken treasures.
- The game is unexplored at first, apart from known islands marked on the map
- Each tile explored may contain a Treasure, a helpful Item, a Hazard or a Monster.
- Collect all ten Treasures to win the game!

## Mechanics
- **Food**: each tile explored takes some time, and the crew of your ship needs feeding
- **Health**: an overall abstraction of crew vitality, hull integrity, and general morale

### Treasures
- Emerald of Hope
- Golden Sword of Yr
- King Flynn's Royal Sceptre
- Sacred Onyx Cross
- Lost Pearl of Jehva
- Queen Latha's Crown
- Ruby Ring of Power
- Silver Chalice of Aunge
- Murphy's Chest of Gold
- Queen Latha's Necklace

### Items
- **Sextant**: highlights all Monster and Hazard tiles within 2 tiles of your ship
- **Tar**: slightly repairs your ship to restore its health
- **Spyglass**: reveals a circle of 7 tiles in a chosen direction
- **Map**: highlights a line to the nearest Treasure tile

### Hazards
- **Iceberg**: damages the ship by a random amount
- **Whirlpool**: removes an acquired Treasure from your possession and rehides it under an unexplored tile

### Monsters
- **Giant Squid**: high-health, low-damage
- **Seaweed Monster**: medium stats but randomly its attacks steal Food
- **Phoenix**: glass cannon Monster; high-damage, low-health. Unlike other Monsters, immediately respawns under an unexplored tile
- **Ghost Ship**: weak to Flaming Arrows, highly resistant to all other weapons
- **Pirate Ship**: only appears once you have any Treasure. Its first attack steals a random Treasure after which it will flee to an unexplored tile. Encountered subsequently will flee after being attacked. Retains damage dealt to it. When defeated, yields its stolen Treasure and does not respawn. Only one ever spawns.

### Weapons
- **Cutlass**: lowest-strength weapon, but does not require any ammunition
- **Cannon**: super-effective against Pirate Ship
- **Giant Axe**: super-effective against Seaweed Monster
- **Flaming Arrows**: super-effective against Ghost Ship
- **Harpoon**: super-effective against Giant Squid
- **Ice Daggers**: super-effective against Phoenix

### Islands
- **Stocked**: islands contain a cache of ammunition for each type of weapon as well as supplies to restore Health
- **Empty**: once visited, islands provide no more benefit


## Core Principles
- **Meaningful trade-offs:** Every safe option should have a real cost. Players should not be able to avoid risk indefinitely with no downside.
- **Fleeing is valid but costly:** Retreat is an intended tactical option, but it should consume resources over time so repeated avoidance is not a dominant strategy.
- **Food as strategic pressure:** Food is the long-run movement economy. Sailing consumes food, and when food reaches zero, movement consumes health instead.
- **Health as immediate survival:** Health is for short-run danger (combat and starvation damage). Health loss should feel urgent and harder to recover than food.
- **Islands as recovery pacing:** Islands provide partial resupply to keep exploration viable while preserving scarcity tension.

## Practical Design Rule
- When adding mechanics that reduce encounter risk (for example, flee, stealth, avoidance), pair them with a counter-pressure in resources, time, or positioning.
