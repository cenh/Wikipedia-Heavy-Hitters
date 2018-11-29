from WikiReader import WikiReader
from Parser import Parser
from CountMinSketch import CountMinSketch
import re
import time
import WordCount
import grequests


def getCategoryMapping(categories, macro_categories):
    req = grequests.get(
        'http://localhost:8081/mapCategory?startCategories='+"::".join(categories)+'&endCategories='+"::".join(macro_categories))
    response = req.send().response.content
    return response.decode()


def log(f, message):
    f.write(message+"\n")


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

        if(cnt > 200):
            break
        cnt += 1

        open(output_file, 'w').close()
        mrJob.run_job()

        categories = re.findall(
            '\[\[Category:.*\]\]', page_dict['revision']['text'])
        # Get all the macro-category matches from the articles categories
        categories = [category.replace("[[Category:", "").replace(
            "]]", "").replace(" ", "_") for category in categories]
        # The assigned macro-category. NOTE: No handling of ties!
        macro = getCategoryMapping(categories, macro_categories)
        if(macro):
            mapping_distribution[macro] += 1
            for word in Parser.getWordsArticle(output_file):
                macroCMS[macro].increment(word[0], word[1])
        if(cnt % 10 == 0):
            log(log_file, "Parsed {} articles so far...".format(cnt))

    log(log_file, "Parsed {} articles in {}s for an average of {}s".format(
        cnt, (time.time()-time_start), (time.time()-time_start)/cnt))

    for cat in macro_categories:
        log(log_file, cat + ": " + str(macroCMS[cat].getHeavyHitters()))
    for cat in macro_categories:
        log(log_file, "{} mapped to {}".format(mapping_distribution[cat], cat))
    #
    log_file.close()
