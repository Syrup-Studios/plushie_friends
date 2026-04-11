## PLUSHIE MOD PLAN

CORE CONCEPT:
3D plushies that behave exactly like vanilla player heads—they "snapshot" a player's skin the moment they are generated.

HOW IT WORKS:
- Skin Logic: Pulls skin data from Mojang only when the plushie is first created.
- The Snapshot: Once generated, the skin is LOCKED. If the player updates their skin later, the plushie does not change.
- Refreshing: To get a plushie with a player's "new" skin, you just generate a new one.

ACQUISITION:
- Commands: Standard syntax: /give @p plushie{SkinOwner:"Name"}
- Loot: Support for datapacks so they can be added to loot chests for modpacks.

THE GOAL:
A simple, lightweight way to have player-specific collectibles that are performance-friendly and don't require constant API syncing.