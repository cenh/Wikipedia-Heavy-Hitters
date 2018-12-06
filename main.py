from WikiReader import WikiReader
from Parser import Parser
from CountMinSketch import CountMinSketch
import re, time, WordCount, grequests, traceback, argparse


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

    tmp_file = "articles/tmp.txt"  # Temporary file used to write to
    output_file = "articles/mr_output.txt"  # The output file from MRJob
    article_list = "articles/articles-list.txt"  # File that MRJob reads from

    parser = argparse.ArgumentParser(description='Main program to get heavy-hitters from Wikipedia.')
    parser.add_argument('--skip', type=int, help='number of articles to skip from start (Default: 0)', default=0)
    parser.add_argument('--parse', type=int, help='total number of articles to parse (Default: 10,000)', default=10000)
    parser.add_argument('--print', type=int, help='number of articles after which every time a log is printed (Default: 100)', default=100)
    parser.add_argument('--result', type=int, help='number of articles after which every time partial results are printed (Default: 100)', default=100)
    parser.add_argument('--input', type=str, help='input .xml file (Default: articles/sample.xml)', default="articles/sample.xml")
    parser.add_argument('--output', type=str, help='output file that contains all the logging (Default: logs.txt)', default="logs.txt")
    args = parser.parse_args()

    wiki_reader = WikiReader(args.input)
    macroCMS = {}
    mapping_distribution = {}
    log_file = open(args.output, 'w', encoding='utf-8')

    for cat in macro_categories:
        macroCMS[cat] = CountMinSketch(
            fraction=0.0005, tolerance=0.0001, allowed_failure_probability=0.01)
        mapping_distribution[cat] = 0

    cnt = 0
    time_start = time.time()
    mrJob = WordCount.WikiWordCount(args=[article_list])
    for page_dict in wiki_reader:
        with open(tmp_file, 'w', encoding='utf-8') as f:
            if page_dict['revision']['text'].startswith('#REDIRECT'):
                continue
            f.write(page_dict['revision']['text'])

        cnt += 1
        if cnt < int(args.skip):
            continue

        if cnt > int(args.parse):
            break

        open(output_file, 'w').close()
        mrJob.run_job()

        categories = re.findall(
            '\[\[Category:.*\]\]', page_dict['revision']['text'])

        # Get all the macro-category matches from the articles categories
        categories = [category.replace("[[Category:", "").replace(
            "]]", "").replace(" ", "_") for category in categories]

        macro = getCategoryMapping(categories, macro_categories)
        if macro in macro_categories :
            mapping_distribution[macro] += 1
            for word in Parser.getWordsArticle(output_file):
                macroCMS[macro].increment(word[0], word[1])
        if cnt % args.print == 0:
            log(log_file, "Parsed {} articles so far...".format(cnt))
        if cnt % args.result == 0:
            log_stats(log_file, macro_categories, cnt, macroCMS, time)

    log_stats(log_file, macro_categories, cnt, macroCMS, time)
    #
    log_file.close()
