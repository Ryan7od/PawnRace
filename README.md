# Pawn Race AI Bot

## Dependencies
This project requires Java and Kotlin

## Overview
This is a bot that plays a variation of chess called Pawn Race. You can mess around with the bot, compile it, and then play it against random, yourself, or another bot.

In its current state, it has a 100% win rate against random.

__*Pawn Race*__ is a variation of chess that only consists of pawns. <br />
The pawns start in the position they normally would, rows 2 and 7. <br />
The aim of the variation is to get 1 pawn to the opponent's side of the board.

## Usage

To compile the code you have written into a playable jar file, in './src' run
```bash
kotlinc . -include-runtime -d ../pawnrace.jar
```
You will then need to move this pawnrace.jar file into a subdirectory in root formatted kotlinpawnrace_{username}, I already have a compiled bot in ro523
<br /><br /><br />
To use the auto-runner, in the root directory you run 
```bash
java -jar -ea pawnrace-autorunner.jar {flags} {n} kotlinpawnrace_{username} {kotlinpawnrace_username2}
```

### {n}
This decides how the auto-runner plays:
 - 0 - The auto-runner plays your compiled bot against an inbuilt random bot
 - \>0 - The auto-runner plays {n} games against another bot, see {kotlinpawnrace_username2}

### {kotlinpawnrace_username}
If this option is set, it will play the bot inside the first folder against the bot inside this given folder.<br />
If it is not set, the bot will play another version of itself.
>Please note that the folder 'pawnrace.jar' is in needs to be formatted kotlinpawnrace_{username}

### {flags}
Here you can put 2 optional flags:
 - --ascii-colours for using 'B' & 'W' instead of Unicode
 - --invert-colours to invert the Unicode characters if you are using a terminal with inverted colours
