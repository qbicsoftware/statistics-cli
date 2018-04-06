# Data Retrieval for Data Representation

This tools is responsible for data retrieval and formatting, in order to visualize data on our homepage.
It strongly collaborates with the statistics portlet (https://github.com/qbicsoftware/qbic-statistics), 
which is responsible for the actual visualization from given data.

## General Remarks
The retrieved data is stored in a YAML file using SnakeYAML. For details on the YAML format, see here: https://github.com/Animosity/CraftIRC/wiki/Complete-idiot%27s-introduction-to-yaml. 
For details on SnakeYAML, see here: https://bitbucket.org/asomov/snakeyaml. 

## Config File format
Each chart needs a config. This config consists of two major parts: 'data' and 'settings' with predefined fields.
### Extend with new fields
If you need to extend them to hold more fields, navigate to the ChartConfig or ChartSetting classes and add your fields.
Be careful with primitive types to not restrict yourself or other users.

If you added new fields, it is VITAL to add all Getters and Setters for SnakeYAML to function properly. 

Sync the eponymic classes in the statistics portlet to ensure proper parsing.
## How to add new data 

In order to add new data to your file, you essentially only need to a add new  class in the package io.queries and call
it properly. The steps are highlighted in the code as TODOs:

1. Create a new class in package io.queries, which implements IQuery:
    In this class you should retrieve your data from the source (OpenBis, GitHub, etc.), format it and 
    create the ChartConfig files for all charts working with that data. 
    Example class: OrganismCountQuery
    
2. Navigate to the enum class ChartNames and add your new chart names. Sync this class with the eponymic class
    in the statistics portlet.

3. Navigate to the MainController and add your query call. 

4. In the MainController add your resulting ChartConfigs to the MainConfig object.

### General Remarks
* Most names of anything are stored in the lexica package as enum classes. In case they change at a certain point,
    they only have to be adjusted in one place.
* There are Helper functions that are likely to be used frequently by different classes. 

