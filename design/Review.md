# Design/Test review notes
## Some things I was thinking while planning out these tests are:
- Does our game really need melee, and stats?  They seem a little out of place in our side-scroller and a lot more to implement and test.
- Same goes with armor.  Just because anything we include will have to be able to be tested, that means our one level will literally be filled with things to pick up so that the Black-box testing can be accomplished.  Instead I recommend a modifier to damage the player takes that is based on defensive power-ups the player finds, or maybe just health power-ups.
- I suggest we get rid of the alternate use of the W key for jump.  We already have SPACE as the jump command, and it could be confusing if the player is trying to jump near a climbing surface.

## Some fine tuning questions I had are:
- Should the player have lives? Or a continue system?
- Will weapons have ammo?  Or like Metroid will it only be alternate weapons that have ammo?
