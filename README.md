# GENA2 Event log generator.

The tool for simulation Petri Nets with inhibitor, reset and weighted arcs.

Supports transition priorities, noise settings, timestamps and resource mapping.

Uses `.pnml` format for Petri Nets. And outputs logs in `.xes` format.

The last release can be found in the github release section.

### Tool usage

For interface mode run in console `java -jar newgena-1.0.jar`

For console mode add the path to the settings json file as a first parameter.

For example: `java -jar newgena-1.0.jar examples/petrinet/complex1/settings.json`

Examples can be found in the folder [examples](examples).
The path to Petri Net and output folder inside the settings is relative to the execution working directory.
So, to run the examples correctly, you should launch the tool near the `examples` folder.  

Full settings description can be found in the file [settings full description.json](examples/petrinet/settings full description.json)