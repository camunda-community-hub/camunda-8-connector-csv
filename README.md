
[![Community badge: Stable](https://img.shields.io/badge/Lifecycle-Stable-brightgreen)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#stable-)
[![Community extension badge](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)

# camunda-8-connector-csv

A connector will be used to manage CSV files


# Principle


## CSV

A CVS is an ASCII file.
The file contains multiple line.
The first line is the definition, and each other line are records, one record per line.
The definition line contains a serie of fields separate by a separator. By default, the separator is a ";".


Example:
````
FirstName;LastName;Email;Address;City:Country
Pierre-Yves;Monnet;pierre-yves.monnet@camunda.com;833 Washington av;San Francisco;USA
Francis;Hulster;francis.hustler@cinema.fr;65 champs Elysée;Paris;France
````
 


## Access the file

The FileStorage library is used.


## Functions available in the connector

* get-properties

Return information on the CSV: number of items, headers, multi format CSV or unique


* read

Read a CSV, and produce a list of Map. A converter can be associate (to transform a String in Integer, or a String in a java.util.date)
A page number, and number of records in the page is given to handle large CSV and protect the reader.
A multi header CSV can be manage by the reader

A filter can be added during the read, via multiple key (read only data where amount > 1000)

* write - append

Write a List of Map in a CSV. A converter can be associated, to transform a java.util.Date to a special string format.
The writer can append data in an existing CSV, and it will respect the format of the existing CSV then.

As option, it can update the CSV to add new columns

* update a CVS

Update an existing CVS with a List of Map. A converter is provided, and correlation keys (for example, the correlation key is 'firstName', 'lastname') to find the record in the existing CSV.


**Flow functions**

These functions do not provide any process variable with data, but process CSV file completely and produce a new CSV file.

* Merge

Merge two CSV files in one file. It can merge only data (assuming the first CSV is the reference) or data and header

* filter

Filter a complete CSV file to a new file.

## Selector

Each function onboard selectors. Selectors filter the input to keep only some data records


## Transformer

Different transformer 


# Get Info

## Principle



csvFunction = `create-user`

![KeycloakUser.png](/doc/KeycloakUser.png)

## Inputs
| Name               | Description                                                                                       | Class             | Default            | Level    |
|--------------------|---------------------------------------------------------------------------------------------------|-------------------|--------------------|----------|
| userRealm          | The user is created in a realm                                                                    | java.lang.String  | `camunda-platform` | REQUIRED |

(1) UserRole: give a string separate by n like "Operate,Tasklist" or "Optimize"

## Output
| Name          | Description                          | Class             | Level    |
|---------------|--------------------------------------|-------------------|----------|
| userId        | Id of user created (or updated)      | java.lang.String  | REQUIRED |

## BPMN Errors

| Name                   | Explanation                                                                        |
|------------------------|------------------------------------------------------------------------------------|
| KEYCLOAK_CONNECTION    | Error arrived during the Keycloak connection                                       |





# Build

```bash
mvn clean package
```

Two jars are produced. The jar with all dependencies can be uploaded in the [Cherry Framework](https://github.com/camunda-community-hub/zeebe-cherry-framework)

## Element Template

The element template can be found in the [element-templates](/element-templates/keycloak-function.json) directory.
