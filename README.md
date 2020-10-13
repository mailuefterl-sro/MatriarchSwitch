# MatriarchSwitch
## General
MatriarchSwitch is a GUI tool to change settings on the [Moog Matriarch](https://www.moogmusic.com/synthesizers?type=202)
Synthesizer.

It helps to
* View and Change settings on a Matriarch connected via MIDI/USB
* Reset all settings to default
* View all changed settings (i.e. non-default settings)
* Create and edit Parameter-sets (sysex files) that can later be downloaded to Matriarch.
## System Requirements
MatriarchSwitch is written in Java and should run on everything that has Java >= 8 installed (Java 11 recommended for HiDPI
support). It has been tested on
* Ubuntu Linux 20.04
* Microsoft Windows 7, 8.1 and 10
* macOS Catalina

## Installation
No installation needed, just make sure Java (8 or newer) is available.
## Running
Double-click the jar file in a file manager, or use the commandline:
`java -jar MatriarchSwitch.jar`
## Usage
1. Select the proper MIDI interface (should be found automatically if Matriarch is connected via USB).
2. Press Button "Retrieve Parameters" to load the current settings from Matriarch
3. Change settings as you like
4. Press "Store Parameters". The program will remind you which parameters you changed, and ask for confirmation before
sending the new settings to Matriarch.

To save settings to a SysEx file that can later be transmitted to Matriarch by any MIDI sender, use File->Export to Sysex file.
You can also import sysex files (that contain ONLY parameter settings), view and edit the changes and re-export the settings.

To see which parameters are non-default on the Matriarch, use: "Retrieve Parameters", Tools->Reset all parameters to default,
"Retrieve Parameters" again. The popup will tell you which parameters are changed.
## Development
If you want to compile the code yourself, you can use maven, Eclipse or plain old javac.
Maven (standalone or within Eclipse) is recommended as it automatically pulls in dependencies and creates full packages.
## License
This code is licensed under MIT style license, i.e. you can use it in your own openSource or commercial project 
without restrictions, just leave the copyright and license notes intact.

Note that the distributed jar file contains dependencies with different licenses, please take a look at
META-INF/LICENSE and META-INF/NOTICE .
