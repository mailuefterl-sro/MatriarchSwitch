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

MacOS should work too, but has not been tested by me.
## Development
If you want to compile the code yourself, you can use maven, Eclipse or plain old javac.
Maven (standalone or within Eclipse) is recommended as it automatically pulls in dependencies and creates full packages.
## License
This code is licensed under MIT style license, i.e. you can use it in your own openSource or commercial project 
without restrictions, just leave the copyright and license notes intact.
