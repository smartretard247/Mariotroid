## Design


#### Basic Controls

##### Keyboard

* W - Jump/Climb
* A - Left
* S - Crouch/Climb
* D - Right
* Space - Jump
* Shift - Sprint
* F - Interact
* I - Inventory

##### Mouse - combat

* Mouse - Aim
* Left Click - Main attack
* Right Click - Alternate attack

##### Mouse - loot/inventory

* Left Click - Select/Drag-and-drop
* Right Click - Pick-up/Drop/Equip

***

#### Layout

##### Layers (refer to layers.jpg for general idea)

The mood I had in mind for this game is somewhat dark with a glimmer of home that our hero can escape and get home.  The colony was destroyed by the calamity in an attempt to take back it's home from the invaders (the colonists).

There are some good (free/open-licensed) textures at https://opengameart.org/

I like [this interface](https://opengameart.org/content/sci-fi-platform-tiles) and [this background](https://opengameart.org/content/industrial-parallax-background) to get us started.

###### Background

* Grayscale, post-apocalyptic, rubble from simple colony structures, simple

* This layer should constantly remind the player of the post-calamity world and all the destruction and misery that befell the hero's world after "the event"

* To maintain a good parallax perspective, this layer should scroll slower than the other two layers

###### Interface

* This is the layer that we can use to mask or texturize the ground, platforms, and other environment objects (both destructible and non-destructible)

* This layer should have very simple textures for the interactables (wood, metal, stone, etc.)

* This layer is the layer that all sprites will traverse

###### Foreground

* Decorative layer to bring more life/immersion to the Interface layer

* This layer should "complete" the interface layer but highlight aspects of the background (nature taking back its planet)

* similar to background, alien foliage, non-interactable forrest critters?, obstructions to hide easter eggs

***

#### Items

##### Weapons

* Crowbar
* Blaster
    * attachments?
* Punch
* Mine/Trap

##### Armor

Can have basic stat modifiers (+n or *n)

* Chest
* Helmet
* Pants
* Boots
* Gloves
* Other (trinket)

##### Utility

* Crowbar (open boxes, break barriers, etc.)
* Jetpack (double jump)
* Health
* Potion (strength, speed, heal over time, etc.)

***

#### Hero

##### Stats

* Strength - affects base power with melee weapons
* Agility - affects base movement speed
* Accuracy - affects base aim dispursion
* Health - max health

##### Actions

* Move
* Climb
* Jump (and double jump)
* Punch
* Shoot
* Break (crowbar)

***

#### Enemies

* Basic enemies (2-3 different types)
* Phantom
* Calamity
