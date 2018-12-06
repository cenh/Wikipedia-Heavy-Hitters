from mrjob.job import MRJob
from mrjob.step import MRStep
# NOTE: You have to do nltk.download('stopwords') for it to work!
from nltk.corpus import stopwords
import re, os

stopWords = set(stopwords.words('english'))
additional_stopWords = {'title', 'name', 'ref', 'http', 'url', 'web', 'cite', 'com', 'publisher', 'www', 'date',
                        'first', 'last1', 'accessdate', 'https', 'journal', 'org', 'year', 'pages', 'last',
                        'issue', 'doi', 'archive', 'name', 'also', 'category', 'file', 'first1', 'htm', 'isbn',
                        'jpg', 'many', 'pdf', 'png', 'abbr', 'usually', 'nbsp', 'html', 'first2', 'last2', 'author',
                        'archiveurl', 'first3', 'last3', 'id', 'including', 'pg', 'archivedate', 'article', 'size',
                        'volume', 'one', 'new', 'center', 'align', 'style', 'text', 'used', 'book', 'books', 'page',
                        'pmid', 'may', 'thumb', 'px', 'news', 'location', 'convert', 'th', 'university', 'website',
                        'google', 'df', 'uk', 'de', 'sfn', 'gov', 'vauthors', 'pmc', 'djdbns', 'mdash', 'en', 'al',
                        'br', 'deadurl', 'access', 'yes', 'edu', 'two', 'work', 'use'}
stopWords = stopWords.union(additional_stopWords)

# Read file containing paths to XML files to read

class PageWordCountProtocol(object):
    BASE_FOLDER = os.getcwd()

    def read(self, filename):
        path = os.path.join(PageWordCountProtocol.BASE_FOLDER, "articles", filename.decode())

        result = []
        with open(path, 'r', encoding='utf-8') as f:
            for line in f:
                if not line.startswith('|'):
                    result.append(line)

        return None, result

    def write(self, key, value):
        with open(PageWordCountProtocol.BASE_FOLDER + 'mr_output.txt', 'a', encoding='utf-8') as f:
            f.write('{} => {}\n'.format(key[1], value))

        return bytes('{word} => {count}'.format(
            word=key[1],
            count=value
        ), 'utf8')


class WikiWordCount(MRJob):
    INPUT_PROTOCOL = PageWordCountProtocol
    OUTPUT_PROTOCOL = PageWordCountProtocol
    reg = re.compile(r'((?!\d+))(\w)+')

    def word_map(self, title, text):
        for line in text:
            for word in WikiWordCount.reg.finditer(line):
                word_lower = word.group(0).lower()
                if word_lower not in stopWords and len(word_lower) > 1:
                    yield (title, word_lower), 1

    def word_reduce(self, key, words):
        yield key, sum(words)

    def steps(self):
        return [
            MRStep(mapper=self.word_map,
                   reducer=self.word_reduce)
        ]

if __name__ == "__main__":
    WikiWordCount.run()
