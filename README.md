# Statistics CLI

[![Build Status](https://travis-ci.com/qbicsoftware/statistics-cli.svg?branch=development)](https://travis-ci.com/qbicsoftware/statistics-cli)[![Code Coverage]( https://codecov.io/gh/qbicsoftware/statistics-cli/branch/development/graph/badge.svg)](https://codecov.io/gh/qbicsoftware/statistics-cli)

Statistics CLI, version 1.0.0-SNAPSHOT - This tool  is responsible for data retrieval and formatting, in order to visualize data on our homepage.
It strongly collaborates with the statistics portlet (https://github.com/qbicsoftware/qbic-statistics), 
which is responsible for the actual visualization from given data.

## General Remarks
The retrieved data is stored in a YAML file using SnakeYAML. For details on the YAML format, see here: https://github.com/Animosity/CraftIRC/wiki/Complete-idiot%27s-introduction-to-yaml. 
For details on SnakeYAML, see here: https://bitbucket.org/asomov/snakeyaml. 

Furthermore, classes are shared between this tool and the visualization portlet (https://github.com/qbicsoftware/statistics-portlet). 
The shared code is incorporated as a dependency. The repo can be found here: https://github.com/qbicsoftware/statistics-plugin.


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
 

## Author
Created by Friederike Hanssen (friederike.hanssen@student.uni-tuebingen.de).

