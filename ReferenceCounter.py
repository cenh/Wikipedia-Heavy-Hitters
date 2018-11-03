from WikiReader import WikiReader
from collections import defaultdict
from enum import Enum
from Parser import Parser
import bs4


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


class ReferencePageItem():
    def __init__(self, ref, count):
        self.reference = ref
        self.referenceCount = count


class RefParser():
    def __init__(self):
        self._refDict = defaultdict()
        self._refList = list()

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

    def parseRefFromText(self, text):
        if("cite web" in text):
            return self.parseWebRef(text)
        if("cite book" in text):
            return self.parseBookRef(text)

    def cleanText(self, text):
        return text.replace("}}", "").strip()

    def parseRef(self, name, text):
        if(type(text) is not bs4.element.NavigableString):
            return

        text = self.cleanText(text)
        if name in self._refDict:
            self._refDict[name].referenceCount += 1
        elif(name):
            ref = self.parseRefFromText(text)
            if(ref):
                refItem = ReferencePageItem(ref, 1)
                self._refDict[name] = refItem
        else:
            ref = self.parseRefFromText(text)
            if(ref):
                self._refList.append(ref)


if __name__ == "__main__":
    input_file = "Wikipedia-20181103100040.xml"
    wiki_reader = WikiReader(input_file)
    refParser = RefParser()
    for page_dict in wiki_reader:
        refs = Parser.getRefs(page_dict['revision']['text'])
        for ref in refs:
            refParser.parseRef(ref.attrs.get('name'), ref.next)
    pass
