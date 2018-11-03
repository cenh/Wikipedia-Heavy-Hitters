import sys
from bs4 import BeautifulSoup

class Parser:
    def getAllPages(file):
        handler = open(file).read()
        soup = BeautifulSoup(handler, 'xml')
        return soup.find_all('page')

    def getText(page):
        soup = BeautifulSoup(page, 'html.parser')
        return soup.title

    def getRefs(text):
        soup = BeautifulSoup(text, 'html.parser')
        return soup.find_all('ref')

if __name__ == "__main__":
    input_file = "Wikipedia-20181103100040.xml"
    pages = Parser.getAllPages(input_file)
    """
    refs = Parser.getRefs(pages[0])
    for ref in refs:
        print(ref)
    """
