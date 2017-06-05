# Design/Test review notes
## Some things I was thinking while planning out these tests are:
- Mitson: Does our game really need melee, and stats?  They seem a little out of place in our side-scroller and a lot more to implement and test.
    - Miller: Melee would be a nice to have in my opinion.  The only reason I wanted to track stats (maybe not visible to the user) is so I can hide a weapon powerup in the game.  Also, I was thinking it would be cool to add an achievement called "Naked" (or something) awarded for beating the boss without the armor that will be placed in the game.  I'm not thinking a full blown stat system like World of Warcraft.  Maybe just a damage reduction stat (ie. Armor reduces damage received to 75%).

- Mitson: Same goes with armor.  Just because anything we include will have to be able to be tested, that means our one level will literally be filled with things to pick up so that the Black-box testing can be accomplished.  Instead I recommend a modifier to damage the player takes that is based on defensive power-ups the player finds, or maybe just health power-ups.
    - Miller: The full suit of armor was just an idea.  For simplicity I think we can just have one armor (whole body) as a loot somewhere.  This armor would reduce incoming damage to 75% or something.
    
- Mitson: I suggest we get rid of the alternate use of the W key for jump.  We already have SPACE as the jump command, and it could be confusing if the player is trying to jump near a climbing surface.
    - Miller: I'm okay with this.  I'll strike this from the design spec.

## Some fine tuning questions I had are:
- Mitson: Should the player have lives? Or a continue system?
    - Miller: I like the lives system like Mario.  We could add an achievement for beating the level without losing any.

- Mitson: Will weapons have ammo?  Or like Metroid will it only be alternate weapons that have ammo?
    - Miller: I think, like Metroid, only the alt weapon will have ammo.  This will be a complicated addition, so for now I think having the main weapon be unlimited is good enough and having no alt weapon until we get to a stable release and want to start adding the "nice to have features"
