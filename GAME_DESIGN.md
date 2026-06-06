# Game Design Principles

This file captures high-level gameplay principles that guide feature work and balancing.

## Core Principles
- **Meaningful trade-offs:** Every safe option should have a real cost. Players should not be able to avoid risk indefinitely with no downside.
- **Fleeing is valid but costly:** Retreat is an intended tactical option, but it should consume resources over time so repeated avoidance is not a dominant strategy.
- **Food as strategic pressure:** Food is the long-run movement economy. Sailing consumes food, and when food reaches zero, movement consumes health instead.
- **Health as immediate survival:** Health is for short-run danger (combat and starvation damage). Health loss should feel urgent and harder to recover than food.
- **Islands as recovery pacing:** Islands provide partial resupply (`+10` food) to keep exploration viable while preserving scarcity tension.

## Practical Design Rule
- When adding mechanics that reduce encounter risk (for example, flee, stealth, avoidance), pair them with a counter-pressure in resources, time, or positioning.
