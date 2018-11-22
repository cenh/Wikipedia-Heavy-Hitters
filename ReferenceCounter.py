from WikiReader import WikiReader
from collections import defaultdict, Counter
from enum import Enum
from Parser import Parser
import bs4, re
from CountMinSketch import CountMinSketch


class Reference():
    def __init__(self):
        self.url = None
        self.title = None
        self.authorLast = None
        self.authorFirst = None


class BookReference(Reference):
    def __init__(self):
        self.year = None
        self.publisher = None
        self.location = None
        self.isbn = None


class WebReference(Reference):
    def __init__(self):
        self.deadUrl = None
        self.accessDate = None
        self.archiveUrl = None
        self.format = None


class JournalReference(Reference):
    def __init__(self):
        pass


class ReferencePageItem():
    def __init__(self, ref, count):
        self.reference = ref
        self.referenceCount = count


class RefParser():
    def __init__(self):
        self.refDict = defaultdict()
        self.refList = list()

    def parseGeneralRef(self, ref, key, value):
        if(key == 'title'):
            ref.title = value
        if(key == 'url'):
            ref.url = value
        if(key == 'first'):
            ref.authorFirst = value
        if(key == 'last'):
            ref.authorLast = value

    def parseWebRef(self, text):
        ref = WebReference()
        attributes = text.split('|')
        for attribute in attributes:
            pair = attribute.split('=')
            if(len(pair)) < 2:
                continue
            key = attribute.split('=')[0].lower()
            value = attribute.split('=')[1].strip()

            self.parseGeneralRef(ref, key, value)
            if(key == 'deadurl'):
                ref.deadUrl = value
            if(key == 'accessdate'):
                ref.accessDate = value
            if(key == 'archiveurl'):
                ref.archiveUrl = value
            if(key == 'format'):
                ref.format = value
        return ref

    def parseBookRef(self, text):
        ref = BookReference()
        attributes = text.split('|')
        for attribute in attributes:
            pair = attribute.strip().split('=')
            if(len(pair)) < 2:
                continue
            key = attribute.split('=')[0].lower().strip()
            value = attribute.split('=')[1].strip()

            self.parseGeneralRef(ref, key, value)
            if(key == 'year'):
                ref.year = value
            if(key == 'publisher'):
                ref.publisher = value
            if(key == 'location'):
                ref.location = value
            if(key == 'isbn'):
                ref.isbn = value
        return ref

    def parseJournalRef(self, text):
        ref = JournalReference()
        attributes = text.split('|')
        for attribute in attributes:
            pair = attribute.strip().split('=')
            if(len(pair)) < 2:
                continue
            key = attribute.split('=')[0].lower().strip()
            value = attribute.split('=')[1].strip()

            self.parseGeneralRef(ref, key, value)
        return ref

    def parseRefFromText(self, text):
        if("cite web" in text):
            return self.parseWebRef(text)
        if("cite book" in text):
            return self.parseBookRef(text)
        if("cite journal" in text):
            return self.parseJournalRef(text)

    def cleanText(self, text):
        return text.replace("}}", "").strip()

    def parseRef(self, name, text):
        if(type(text) is not bs4.element.NavigableString):
            return

        text = self.cleanText(text)
        if name in self.refDict:
            self.refDict[name].referenceCount += 1
        elif(name):
            ref = self.parseRefFromText(text)
            if(ref):
                refItem = ReferencePageItem(ref, 1)
                self.refDict[name] = refItem
        else:
            ref = self.parseRefFromText(text)
            if(ref):
                self.refList.append(ref)


if __name__ == "__main__":
    macro_categories = ["Arts", "Concepts", "Culture", "Education", "Events", "Geography", "Health", "History", "Humanities",
                        "Language", "Law", "Life", "Mathematics", "Nature", "People", "Philosophy", "Politics", "Reference",
                        "Religion", "Science", "Society", "Sports", "Technology", "Universe", "World"]
    shortest_path_file = "ShortestPaths.txt"
    input_file = "Wikipedia-20181103100040.xml"
    wiki_reader = WikiReader(input_file)
    refParsers = list()
    macroCMS = {}

    # Load shortest paths into a dict
    shortestPaths = Parser.getShortestPaths(shortest_path_file)

    # Create a CMS for every macro category
    for cat in macro_categories:
        macroCMS[cat] = CountMinSketch(10, 1/(20), 0.01)

    # Go through the pages
    for page_dict in wiki_reader:
        # Get all the references and categories
        refs = Parser.getRefs(page_dict['revision']['text'])
        categories = re.findall('\[\[Category:.*\]\]', page_dict['revision']['text'])

        # Get all the macro-category matches from the articles categories
        matches = [shortestPaths[category.replace("[[Category:", "").replace("]]", "").replace(" ", "_")] for category in categories]
        # The assigned macro-category. NOTE: No handling of ties!
        macro = Parser.solveMatches(matches)

        refParser = RefParser()
        refParsers.append(refParser)
        for ref in refs:
            refParser.parseRef(ref.attrs.get('name'), ref.next)

    """
    countDict = Counter()
    for refParsed in refParsers:
        for refKey in refParsed.refDict.keys():
            ref = refParsed.refDict[refKey].reference
            if(type(ref) is BookReference and ref.isbn):
                isbn = ref.isbn.replace("-", "")
                for i in range(refParsed.refDict[refKey].referenceCount):
                    countMinSketch.increment(isbn)
                    countDict[isbn] += 1
        for ref in refParsed.refList:
            if(type(ref) is BookReference and ref.isbn):
                isbn = ref.isbn.replace("-", "")
                countMinSketch.increment(isbn)
                countDict[isbn] += 1

    error = 0
    errorCount = 0

    for isbn in countDict.keys():
        occurrences = countDict[isbn]
        estimate = countMinSketch.count(isbn)
        error = abs(occurrences-estimate)
        errorCount += 1
    print("error: {}".format(error/errorCount))
  
    """

