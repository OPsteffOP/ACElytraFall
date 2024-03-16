## Download
https://github.com/OPsteffOP/ACElytraFall/releases/tag/v1

## Setup
- /acelytrafall arena create {arena_name} : creates the arena with your location as the start point.
- /acelytrafall arena delete  {arena_name} : deletes the arena along with respective join signs.

Join signs can be created by writing the following lines on a sign:
- Line 1: [ACElytraFall]
- Line 2: {arena_name}

All these actions require either OP or the "acelytrafall.admin" permission.

> [!NOTE] 
> The plugin detects portal enter events as a player reaching the end of the arena.



https://github.com/OPsteffOP/ACElytraFall/assets/22009529/f581936f-3f34-4ab0-8ff8-954d2e139b63



## Features
- Player data can be viewed with "/acelytrafall data {arena_name} {player_name}". This command requires OP or "acelytrafall.admin" permission to be used.
- Players can see a hologram of their own (or the global arena) record. They can change there hologram view type by using "/acelytrafall config record_hologram {hologram_type}". This command only works while they're in an arena as it's specific for that arena. The hologram types are:
	- NONE: this will no longer show a hologram.
	- PERSONAL_RECORD: this will show a hologram of their own personal record.
	- GLOBAL_RECORD: this will show a hologram of the global arena record.
- Players will see a scoreboard when they join an arena, showing the map name, their hologram type, their elapsed time, and information on how to leave the minigame (/acelytrafall leave). The elapsed time will also be shown in the action bar above their hotbar.
- Player's won't be able to execute commands except for the ones that start with anything specified in "allowed_commands.yml". They'll also always be able to execute the /acelytrafall commands.
	- The commands in the allowed_commands.yml file should be separated by a new line.

Example of allowed_commands.yml:
> /test_command<br>
> /another_command<br>
> /command_with_args hello<br>

Example of usages with this allowed_commands.yml:

> :heavy_check_mark: /test_command<br>
> :heavy_check_mark: /test_command subcommand<br>
> :heavy_check_mark: /command_with_args hello<br>
> :heavy_check_mark: /command_with_args hello world<br>
> :x: /command_with_args subcommand<br>

## Important notes

 - Player and arena data is loaded and saved asynchronously:
	 - Keeps track of the player's location data (along with a few other variables) each game tick to later simulate their record flight if they choose the PERSONAL_RECORD record hologram view type. (see RecordPlayerHologram). When the player breaks their own record for an arena, all this data is stored in their player file. Loading and saving this data can be slow depending on the amount which is why I preferred to do this asynchronously to prevent hanging the server and causing interruptions for other players.
	 - The plugin also keeps track of the arena record time, along with all the movement data to reconstruct their flight if a player chooses the GLOBAL_RECORD record hologram view type.
 - Player data is kept in memory for some time after the player leaves the arena:
	 - Since loading the player data can be a slow process, I wanted to try and minimize the need for this. When the player leaves the arena, their data is scheduled for deletion out of memory unless they join back within the specified time (see Arena.scheduledUnloadPlayers).

> [!IMPORTANT] 
> The plugin will work optimally in 1.20.4 due to the record hologram code using net.minecraft.server classes. The record holograms are only implemented for this version. The plugin will disable them in other version and continue to work.

## Gameplay



https://github.com/OPsteffOP/ACElytraFall/assets/22009529/27d0c732-469b-449c-9db7-e0fb9490960a



## Credits
The map used in the videos is Noxcrew's Terra Swoop Force: https://noxcrew.com/creations/terraswoopforce
