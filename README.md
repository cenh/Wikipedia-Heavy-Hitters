# Wikipedia-Heavy-Hitters
This repository contains the code solving the problem of finding the most common words used in the 25 top-level categories.

## Table of Contents
- [Introduction](#introduction)
- [Installation](#installation)
- [Usage](#usage)
- [Results](#results)
- [License](#license)

## Introduction

Wikipedia has almost 6 million articles in the English language, and around 1.7 million categories. Some of these categories assigned to the articles are very abstract, and does not tell the reader what the core subject of the article is. However Wikipedia has 25 top-level categories (Refered to as macro-categories) such as _Arts_ and _History_, being able to assign one of these categories to an article, would give the reader a better idea of the subject explored in the article. Using these macro-categories, one could find the most common words used within a specific field, and finding these words could then later be used to classify articles that has not been assigned any categories at all.
Our work will focus on assigning one of these 25 macro-categories to an article, and then finding the most common words used in all the macro-categories.

## Installation
Installation guide

### Python
    pip install -r requirements.txt

Open a python shell and type:

    import nltk
    nltk.download('stopwords')
    
You should now be able to run main.py

### Java

Make sure maven is installed on the host


The java program root folder is [this one](./wikipedia-graph-neo4j/).

Open the terminal in this folder and run the following command
    
    mvn package

Install Neo4j server and make sure it is running the bolt protocol on port 7687.

## Usage

### Creating the category database

Download the sql dumps of these wikipedia tables [from the official page](https://dumps.wikimedia.org/enwiki/): category, categorylinks, page.

Change directory into the target directory (./wikipedia-graph-neo4j/target) and run:

    java -jar .\wikipedia-graph-neo4j-0.0.1-SNAPSHOT.jar 
    --spring.profiles.active=create-wiki-graph-db 
    --category-dump-file=<path to category file> 
    --page-dump-file=<path to page file>
    --category-links-dump-file=<path to categorylinks file>
    --base-folder=<the folder where the program outputs files>

Make sure to replace the placeholder <> with the paths to the downloaded files.
This process takes several hours.

### Exposing the HTTP interface used by the Python program

Simply run

    java -jar .\wikipedia-graph-neo4j-0.0.1-SNAPSHOT.jar

By default it will be listening on localhost:8080, exposing some APIs.

Example of article mapping http request:

    http://localhost:8080/mapCategory?startCategories=Database_management_systems::Databases&endCategories=Arts::Geography::Technology::Science::People::World

The query params start-category represents the categories the articles has, the end-categories represent the macro-category. One of the macro-category will be returned as result of the mapping. More on this algorithm of mapping can be read in the [final paper](./CTDS___Heavy_Hitters_Words.pdf).

Example of shortest path http request:

    http://localhost:8080/shortestPath?startCategory=Database_management_systems&endCategory=Arts&maxPathLength=10

### Heavy Hitters

When the graph has been created and the Java program is running exposing the HTTP interface, you can then run main.py.

The program supports the following:

```
usage: main.py [-h] [--skip SKIP] [--parse PARSE] [--print PRINT]
               [--result RESULT] [--input INPUT] [--output OUTPUT]

Main program to get heavy-hitters from Wikipedia.

optional arguments:
  -h, --help       show this help message and exit
  --skip SKIP      number of articles to skip from start (Default: 0)
  --parse PARSE    total number of articles to parse (Default: 10,000)
  --print PRINT    number of articles after which every time a log is printed
                   (Default: 100)
  --result RESULT  number of articles after which every time partial results
                   are printed (Default: 100)
  --input INPUT    input .xml file (Default: articles/sample.xml)
  --output OUTPUT  output file that contains all the logging (Default:
                   logs.txt)

```

## Results
A showcase of our results after parsing 8600 articles. The results can be generated using the stats.py, it uses the log of the main.py to generate the results. Only use the last block of the output (See or use logs.txt as an example).

### Distribution
Showing the distribution of articles being assigned to the 10 largest macro-categories.
![alt text](https://github.com/cenh/Wikipedia-Heavy-Hitters/blob/master/images/Distribution.png?raw=true "Distribution among the 10 largest categories")

### Heavy-hitters
The 10 largest heavy-hitters for "Sports" and "Technology" respectively:
![alt text](https://github.com/cenh/Wikipedia-Heavy-Hitters/blob/master/images/Sports.png?raw=true "10 largest heavy-hitters for Sports")
![alt text](https://github.com/cenh/Wikipedia-Heavy-Hitters/blob/master/images/Technology.png?raw=true "10 largest heavy-hitters for Technology")

### Unique heavy-hitters:
The unique that only appears for a certain category.

For Sports:
    
    {'cup', 'football', 'season', 'league', 'baseball', 'team'}

For Technology:
    
    {'computer', 'system', 'company', 'apple', 'data'}

## License
The package is Open Source Software released under the [MIT](LICENSE) license.
