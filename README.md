# CS22120 Group Project 6

## Contents

1. [Description](#description)
   - [Controls](#controls)
2. [Current Status](#current-status)
   - [Known Issues](#known-issues)
3. [Team](#team)
4. [How To Build And Run](#how-to-build-and-run)
   - [Build With IntelliJ](#build-with-intellij)
5. [Credit For Sources](#credits-for-sources)

## Description

Producing a high-quality software ‘product’ that is specified, designed, implemented, tested and documented to professional standards.

This 'product' will be an JavaFX Deck of cards application, which can be accessed and played to any game specifications given.

### Controls:

| Action                                   | Mouse Controls                                                                                                                                        | Keyboard Controls            |
|------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------|
| Next menu button (any screen)            |                                                                                                                                                       | Tab                          |
| Previous menu button (any screen)        |                                                                                                                                                       | Shift+Tab                    |
| Press menu button                        | Click on the button                                                                                                                                   | Enter                        |
| Clear selected menu button               |                                                                                                                                                       | Escape                       |
| Select Card/Deck                         | Right click on card/deck                                                                                                                              |                              |
| Select multiple cards/decks individually | Ctrl+Click on each card/deck individually                                                                                                             |                              |
| Select multiple cards/decks at once      | Click and drag so that the selection box covers everything you want to select                                                                         |                              |
| Deselect individual cards/decks          | Ctrl+Click on each selected card/deck to deselect                                                                                                     |                              |
| Deselect all cards/decks                 |                                                                                                                                                       | C                            |
| Move card(s)/deck(s)                     | Click and drag on a card/deck to move it, all selected cards/decks will move                                                                          | W A S D = Up Left Down Right |
| Draw top card of deck                    | Right click and drag off the deck                                                                                                                     |                              |
| Open actions popup                       | Right click on a card/deck, or optionally on the table if multiple are selected                                                                       |                              |
| Place a card on a deck                   | Move a card onto a deck by dragging it on top of the deck                                                                                             |                              |
| Place card(s)/deck(s) on a card/deck     | Select any number of cards/decks, open the action popup for a card/deck that is not selected by right clicking on it, then click place on deck        |                              |
| Flip card(s)                             | Open action popup and click "Flip"                                                                                                                    | F                            |
| Reveal card(s) (not implemented)         | Open action popup and click "Reveal"                                                                                                                  | R                            |
| Shuffle deck(s)                          | Open action popup and click "Shuffle"                                                                                                                 | G                            |
| Reposition top card of deck to middle    | Open action popup and click "Reposition Top" then "To Middle"                                                                                         |                              |
| Reposition top card of deck to bottom    | Open action popup and click "Reposition Top" then "To Bottom"                                                                                         |                              |
| Open/Close table menu                    | Click the 3 lines in the top right corner of the screen                                                                                               | Escape                       |
| Record dealing layout                    | Open the table menu and click the first option, showing 3 cards<br/>From that point on, any card draws from the first deck you draw from are recorded | Enter while selected         |
| End dealing layout recording             | Open the table menu and click the first option again, showing 3 cards                                                                                 | Enter while selected         |
| Record game                              | Open the table menu and click the second option, showing a camera<br/>From that point on, all actions are recorded                                    | Enter while selected         |
| End game recording                       | Open the table menu and click the second option again, showing a camera                                                                               | Enter while selected         |
| Quit                                     | Open the table menu and click the fifth option, showing a door                                                                                        | Enter while selected         |

## Current Status

Complete (Including Documentation):
- US1
- US2
- US3
- US4
- US5
- US6
- US7
- US8
- US9
- US10
- US11
- US12
- US13
- US14
- US15
- US16

Unit Tested:
- No Incomplete Stories

Implemented:
- No Incomplete Stories

Partially Implemented:
- No Incomplete Stories

Not Started Implementing:
- US17
- US18

### Known Issues

- Flipping/revealing cards does not conserve their rotation
- Cards can be dragged off-screen in various ways and are impossible to recover
- Right click popups on the table are not keyboard navigable, nor can you select cards/decks with the keyboard
- Cannot use keyboard controls on hovered cards/decks
- Exiting fullscreen can break things, but is only possible on mac/linux in testing
- In a replay, manually skipping forward whilst a reveal action is in progress will break the replay
- Returning to menu after finishing a replay then starting another replay ends the replay instantly
- Drawing the second last card of a deck can result in bugs with a card following the mouse for no reason and an invisible deck that you can stack cards onto to vanish them

## How To Build And Run

> **Aberystwyth University M Drive**
> 
> Note that on Aberystwyth University systems, as of the time of writing (06/03/2025), I have been unable to get any JavaFX program on the M drive to run through any previous method, although I have yet to test since migrating to maven. All attempts have compiled then failed to run with the following error:
> 
> ```bash
> Graphics Device initialization failed for :  d3d, sw
> Error initializing QuantumRenderer: no suitable pipeline found
> java.lang.RuntimeException: java.lang.RuntimeException: Error initializing QuantumRenderer: no suitable pipeline found
>     ...
> ```
> 
> **Until such a time as I figure out how to resolve this, please do not attempt to use the Aber Uni M drive.**

---

### Build With IntelliJ

Clone the repository with `git clone https://github.com/Premar19/Software-Engineering-Project.git`.

Open the project in IntelliJ and head to the run configurations in the top right (to the left of the run button). Click the dropdown, click `Edit Configurations`, click the `+` and select `Maven`. In the run box that says `Command Line`, enter `clean javafx:run -f pom.xml`. If that run configuration does not work as-is, add another using the same steps that just says `install` in the box, run that, then run the previous run configuration again.

To run the unit tests, repeat the steps above, but instead enter `clean test -f pom.xml` into the text box.

With that, you should be able to run the project with no issues.

---

## Credits For Sources

Below are all credits for sources used:
- https://docs.oracle.com/javafx/2/api/
- https://www.overleaf.com/learn
- https://stackoverflow.com/a/72817274 - Making `mvn clean` not delete user uploaded images
- https://sequencediagram.org/
- https://www.websequencediagrams.com/
