from WikiReader import WikiReader
from Parser import Parser
from CountMinSketch import CountMinSketch
import re
import time
import WordCount
import grequests
from sys import argv
import sys
import traceback


def getCategoryMapping(categories, macro_categories):
    try:
        req = grequests.get(
            'http://localhost:8081/mapCategory?startCategories='+"::".join(categories)+'&endCategories='+"::".join(macro_categories))
        response = req.send().response.content
        return response.decode()
    except Exception:
        print("Error in contacting the http mapper interface\n" +
              "{0}".format(traceback.print_exc()))


def log(f, message):
    f.write(message+"\n")
    f.flush()


def log_stats(f, macro_categories, cnt, macroCMS, time):
    log(f, "Parsed {} articles in {}s for an average of {}s".format(
        cnt, (time.time()-time_start), (time.time()-time_start)/cnt))

    for cat in macro_categories:
        log(f, cat + ": " + str(macroCMS[cat].getHeavyHitters()))
    for cat in macro_categories:
        log(f, "{} mapped to {}".format(mapping_distribution[cat], cat))


if __name__ == "__main__":
    macro_categories = ["Arts", "Concepts", "Culture", "Education", "Events", "Geography", "Health", "History", "Humanities",
                        "Language", "Law", "Life", "Mathematics", "Nature", "People", "Philosophy", "Politics", "Reference",
                        "Religion", "Science", "Society", "Sports", "Technology", "Universe", "World"]
    #dataset_file = "articles/Wikipedia-20181103100040.xml"
    dataset_file = "/var/articles.xml"
    tmp_file = "articles/tmp.txt"  # Temporary file used to write to
    output_file = "articles/mr_output.txt"  # The output file from MRJob
    article_list = "articles/articles-list.txt"  # File that MRJob reads from
    #
    if(len(argv) < 5):
        print("Error: 4 arguments required")
        print("Argument 0: number of articles to skip from start")
        print("Argument 1: total number of articles to parse")
        print("Argument 2: number of articles after which every time a log is printed")
        print("Argument 3: number of articles after which every time partial results are printed")
        exit()
    #
    wiki_reader = WikiReader(dataset_file)
    macroCMS = {}
    mapping_distribution = {}
    log_file = open("logs.txt", 'w', encoding='utf-8')
    #
    for cat in macro_categories:
        macroCMS[cat] = CountMinSketch(
            fraction=0.0005, tolerance=0.0001, allowed_failure_probability=0.01)
        mapping_distribution[cat] = 0

    cnt = 0
    time_start = time.time()
    mrJob = WordCount.WikiWordCount(args=[article_list])
    for page_dict in wiki_reader:
        with open(tmp_file, 'w', encoding='utf-8') as f:
            if(page_dict['revision']['text'].startswith('#REDIRECT')):
                continue
            f.write(page_dict['revision']['text'])

        cnt += 1
        if(cnt < int(argv[1])):
            continue

        if(cnt > int(argv[2])):
            break

        open(output_file, 'w').close()
        mrJob.run_job()

        categories = re.findall(
            '\[\[Category:.*\]\]', page_dict['revision']['text'])
        # Get all the macro-category matches from the articles categories
        categories = [category.replace("[[Category:", "").replace(
            "]]", "").replace(" ", "_") for category in categories]
        # The assigned macro-category. NOTE: No handling of ties!
        macro = getCategoryMapping(categories, macro_categories)
        if(macro in macro_categories):
            mapping_distribution[macro] += 1
            for word in Parser.getWordsArticle(output_file):
                macroCMS[macro].increment(word[0], word[1])
        if(cnt % int(argv[3]) == 0):
            log(log_file, "Parsed {} articles so far...".format(cnt))
        if(cnt % int(argv[4]) == 0):
            log_stats(log_file, macro_categories, cnt, macroCMS, time)

    log_stats(log_file, macro_categories, cnt, macroCMS, time)
    #
    log_file.close()
