from bs4 import BeautifulSoup
from collections import defaultdict, Counter

class Parser:
    @staticmethod
    def getWordsArticle(file):
        words = []
        with open(file, encoding='utf-8') as f:
            for line in f:
                line = line.split(" => ")
                word = line[0].replace("#", "")
                count = int(line[1].replace("\n", ""))
                words.append((word, count))
        return words

    @staticmethod
    def getAllPages(file):
        handler = open(file).read()
        soup = BeautifulSoup(handler, 'xml')
        return soup.find_all('page')

    @staticmethod
    def getText(page):
        soup = BeautifulSoup(page, 'html.parser')
        return soup.title

    @staticmethod
    def getRefs(text):
        soup = BeautifulSoup(text, 'html.parser')
        return soup.find_all('ref')

    @staticmethod
    def solveMatches(matches):
        count = Counter([x[0] for x in matches]).most_common()

        # If there is no tie
        if count[0][1] != count[1][1]:
            return count[0][0]

        # We only want to look at categories with the same numbers of appearance as the most common one
        max_cats = [match[0] for match in count if match[1] == count[0][1]]

        # If there is a tie we sum the distances and take the shortest
        # If there is a tie between the summed distances, we just take the last one
        distance_dict = defaultdict(int)
        for k, v in matches:
            if k in max_cats:
                distance_dict[k] += v
        return Counter(distance_dict).most_common()[-1][0]
