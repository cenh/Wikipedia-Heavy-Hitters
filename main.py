from WikiReader import WikiReader
from Parser import Parser
from CountMinSketch import CountMinSketch
import re, time, WordCount

if __name__ == "__main__":
    macro_categories = ["Arts", "Concepts", "Culture", "Education", "Events", "Geography", "Health", "History", "Humanities",
                        "Language", "Law", "Life", "Mathematics", "Nature", "People", "Philosophy", "Politics", "Reference",
                        "Religion", "Science", "Society", "Sports", "Technology", "Universe", "World"]
    dataset_file = "articles/Wikipedia-20181103100040.xml"

    tmp_file = "articles/tmp.txt" # Temporary file used to write to
    output_file = "articles/mr_output.txt" # The output file from MRJob
    article_list = "articles/articles-list.txt" # File that MRJob reads from

    wiki_reader = WikiReader(dataset_file)

    macroCMS = {}
    for cat in macro_categories:
        macroCMS[cat] = CountMinSketch(fraction=0.0005, tolerance=0.0001, allowed_failure_probability=0.01)

    cnt = 0
    time_start = time.time()
    mrJob = WordCount.WikiWordCount(args=[article_list])
    for page_dict in wiki_reader:
        cnt += 1
        with open(tmp_file, 'w') as f:
            f.write(page_dict['revision']['text'])

        open(output_file, 'w').close()
        mrJob.run_job()

        categories = re.findall('\[\[Category:.*\]\]', page_dict['revision']['text'])
        # Get all the macro-category matches from the articles categories
        #matches = [shortestPaths[category.replace("[[Category:", "").replace("]]", "").replace(" ", "_")] for category in categories]
        # The assigned macro-category. NOTE: No handling of ties!
        #macro = Parser.solveMatches(matches)
        macro = "Nature"

        for word in Parser.getWordsArticle(output_file):
            macroCMS[macro].increment(word[0], word[1])

    print("Parsed {} articles in {}s for an average of {}s".format(cnt, (time.time()-time_start), (time.time()-time_start)/cnt))
    print(macroCMS['Nature'].getHeavyHitters())

