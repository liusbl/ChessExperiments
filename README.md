## Goals of this project
The current goal is to brute-force analyze all the possible King, King, Queen (KKQ) endgame positions and **make determinations about premove possibilities** 

## Problems:
- [ ] [Trivial] In KKQ endgame, is it possible to **MOVE** until checkmate from any position? Note: this is a known truth, but not yet answered by this program.
- [ ] In KKQ endgame, is it possible to **PREMOVE** until checkmate from any position? Note: this is a known truth, but not yet answered by this program.
- [ ] In KKQ endgame, is it possible to **PREMOVE twice, thrice or n times** until checkmate from any position?
- [ ] Could you premove with a Rook to checkmate? 
- [ ] What if it wasn't a Queen, but some more advanced piece, like an Empress, which can move like a Queen and a Knight. What would be it's premoving-to-checkmate capabilities?
  - [ ] What if it were a Queen which can teleport to any square but only capture and check in the style of a Queen? 

## Additional curiosities:
- [ ] [When there are more pieces] How many disconnected/unreachable board states there can be? 

### Testing the project
1. Run `creation/main.kt` to create all the possible board configurations of the selected size 
2. Run `analysis/main.kt` to analyze the next best moves


