# Data Retrieval for Data Representation

This tools is responsible for data retrieval and formatting, in order to visualize data on our homepage.
It strongly collaborates with the statistics portlet (https://github.com/qbicsoftware/qbic-statistics), 
which is responsible for the actual visualization from given data.

## General Remarks
The retrieved data is stored in a YAML file using SnakeYAML. For details on the YAML format, see here: https://github.com/Animosity/CraftIRC/wiki/Complete-idiot%27s-introduction-to-yaml. 
For details on SnakeYAML, see here: https://bitbucket.org/asomov/snakeyaml. 

Furthermore, classes are shared between this tool and the visualization portlet (https://github.com/qbicsoftware/qbic-statistics). 
The shared code is incorporated as a submodule. The submodules repo can be found here: https://github.com/qbicsoftware/qbic-statistics-core.


## Config File format
Each chart needs a config. This config consists of two major parts: 'data' and 'settings' with predefined fields. 
### Extend with new fields
If you need to extend them to hold more fields, the shared code base needs to be adapted. It is visible 
in this repo under the package submodule. However, it should not be modified here, but rather in its own repo. See below on how to modify classes in a submodule. 
Fields can be adapted in the classes ChartConfig and ChartSetting.
Be careful with primitive types to not restrict yourself or other users.

If you added new fields, it is VITAL to add all Getters and Setters for SnakeYAML to function properly. 

## How to add new data 

In order to add new data to your file, you essentially only need to add a new class in the package io.queries and call
it properly. The steps are highlighted in the code as TODOs:

1. Create a new class in package io.queries, which implements IQuery:
    In this class you should retrieve your data from the source (OpenBis, GitHub, etc.), format it and 
    create the ChartConfig files for all charts working with that data. 
    Example class: OrganismCountQuery
    
2. Adapt the enum class ChartNames in the submodule and add your new chart names. (See below)

3. Navigate to the MainController and add your query call. 

4. In the MainController add your resulting ChartConfigs to the MainConfig object.

## How to change classes in the submodule

In order to change classes from the submodule, clone the repo at https://github.com/qbicsoftware/qbic-statistics-core
and edit the classes as you like. When you are done, commit your changes to the master branch as usual.

After commiting your changes to the qbic-statistics core repo, navigate into your local copy of statistics-data-retrieval 
and execute the following command:

``git submodule foreach git pull origin master``

The submodule classes are now updated automatically. All other repos that implement this submodule receive the same
updates, if the above command is called. Otherwise, your submodule will point to the time-point in history, it was updated last. 

### General Remarks
* Most names of anything are stored in the lexica package as enum classes. In case they change at a certain point,
    they only have to be adjusted in one place.
* There are Helper functions that are likely to be used frequently by different classes. 

