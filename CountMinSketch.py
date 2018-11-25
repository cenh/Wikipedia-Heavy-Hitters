import mmh3
import math
import numpy as np

class CountMinSketch:
    def __init__(self, fraction, tolerance, allowed_failure_probability, iterator=1000):
        self.fraction = fraction
        self.tolerance = tolerance
        self.hash_count = round(math.log(1/allowed_failure_probability))
        self.allowed_failure_probability = allowed_failure_probability
        self.columns = round(math.exp(1)/tolerance)
        self.matrix = np.zeros((self.hash_count, self.columns))
        self.heavyHitters = {}
        self.cnt = 0
        self.iterator = iterator
	
    def increment(self, url):
        self.cnt += 1
        for seed in range(self.hash_count):
            index = mmh3.hash(url, seed)
            self.matrix[seed, index % self.columns] += 1

        if self.cnt % self.iterator is 0:
            self.updateHeavyHitters()

        if (self.count(url)/self.cnt) > self.fraction:
            self.heavyHitters[url] = self.count(url)

    def count(self, url):
        minv = None
        for seed in range(self.hash_count):
            index = mmh3.hash(url, seed)
            if seed == 0 or minv > self.matrix[seed, index % self.columns]:
                minv = self.matrix[seed, index % self.columns]
        return minv

    def getHeavyHitters(self):
        self.updateHeavyHitters()
        return self.heavyHitters

    def updateHeavyHitters(self):
        for k, v in self.heavyHitters.copy().items():
            if not (v / self.cnt) > self.fraction:
                del self.heavyHitters[k]

if __name__ == "__main__":
    CMS = CountMinSketch(0.05, 0.01, 0.001, 5)
    list = ["Cat", "Cat", "Cat", "Mouse", "Mouse", "Cat", "Cat", "Horse", "Chicken", "Dog", "Duck", "Spider", "Eagle",
            "Bird", "Fish", "Salmon", "Horse", "Dog", "Scorpion", "Lion", "Tiger", "Giraffe", "Monkey", "Bear", "Cow",
            "Bull", "Bizon", "Snake", "Horse", "Horse", "Dog", "Lion", "Lion", "Lion", "Cow", "Monkey", "Bear", "Eagle",
            "Bird", "Fish", "Salmon", "Scorpion", "Duck", "Spider", "Tiger", "Chicken"]
    for item in list:
        CMS.increment(item)
    print(CMS.getHeavyHitters())
