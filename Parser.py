from bs4 import BeautifulSoup
from collections import defaultdict, Counter

class Parser:
    @staticmethod
    def getShortestPaths(file):
        dict = {}
        with open(file) as f:
            for line in f:
                ls = line.replace("\n", "").split("=>")
                start_node = ls[0].replace(" ", "")
                ls = ls[1].split(":")
                end_node = ls[0].replace(" ", "")
                distance = int(ls[1].replace(" ", ""))
                dict[start_node] = (end_node, distance)
        return dict

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
        count = Counter([x[0] for x in matches]).most_common(2)

        # If there is no tie
        if count[0][1] != count[1][1]:
            return count[0][0]

        # If there is a tie we sum the distances and take the shortest
        # If there is a tie between the summed distances, we just take the last one
        distance_dict = defaultdict(int)
        for k, v in matches:
            distance_dict[k] += v
        return Counter(distance_dict).most_common()[-1][0]



if __name__ == "__main__":
    input_file = "ShortestPaths.txt"
    shortest_path = Parser.readShortestPaths(input_file)
    print(shortest_path)
    """
    refs = Parser.getRefs(pages[0])
    for ref in refs:
        print(ref)
    """
