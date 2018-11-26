from mrjob.job import MRJob
from mrjob.step import MRStep
from WikiReader import WikiReader
import re
import os

# Read file containing paths to XML files to read
class PageWordCountProtocol(object):
    BASE_FOLDER = "C:/Users/xents/Documents/DTU/Cours/S3/ComputationalToolsForDatasScience/Project/articles/"

    def read(self, filename):
        path = PageWordCountProtocol.BASE_FOLDER + filename.decode()

        print("Reading ", path)

        wiki = WikiReader(path)

        result = [] # List of all the articles
        for page in wiki:
            result.append(page)

        return None, result

    def write(self, key, value):
        return bytes('{title}#{word} => {count}'.format(
            title=key[0],
            word=key[1],
            count=value
        ), 'utf8')


class WikiWordCount(MRJob):
    INPUT_PROTOCOL = PageWordCountProtocol
    OUTPUT_PROTOCOL = PageWordCountProtocol
    reg = re.compile(r'((?!\d+))(\w)+')

    def extract_articles(self, _, pages):
        for page in pages:
            yield page['title'], page['revision']['text']

    def word_map(self, title, text):
        for word in WikiWordCount.reg.finditer(text):
            yield (title, word.group(0)), 1

    def word_reduce(self, key, words):
        yield key, sum(words)

    def steps(self):
        return [
            MRStep(mapper=self.extract_articles),
            MRStep(mapper=self.word_map,
                   reducer=self.word_reduce)
        ]


if __name__ == "__main__":
    WikiWordCount.run()