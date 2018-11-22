import sys
from bs4 import BeautifulSoup


class Parser:
    @staticmethod
    def calculateShortestPaths(file):
        dict = {}
        with open(file) as f:
            for line in f:
                ls = line.replace("\n", "").split(":")
                start_node = ls[0].replace(" ", "")
                end_node = ls[1].replace(" ", "")
                distance = int(ls[2].replace(" ", ""))
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


if __name__ == "__main__":
    input_file = "ShortestPaths.txt"
    shortest_path = Parser.readShortestPaths(input_file)
    print(shortest_path)
    """
    refs = Parser.getRefs(pages[0])
    for ref in refs:
        print(ref)
    """
