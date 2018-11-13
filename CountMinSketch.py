from bitarray import bitarray
import mmh3
import math
import numpy as np

class CountMinSketch:
    def __init__(self, fraction, tolerance, allowed_failure_probability):
        self.fraction = fraction
        self.tolerance = tolerance
        self.hash_count = round(math.log(1/allowed_failure_probability))
        self.allowed_failure_probability = allowed_failure_probability
        self.columns = round(math.exp(1)/tolerance)
        self.matrix = np.zeros((self.hash_count,self.columns))
	
    def increment(self, url):
        for seed in range(self.hash_count):
            index = mmh3.hash(url, seed)
            self.matrix[seed, index % self.columns] += 1

    def count(self, url):
        minv = None
        for seed in range(self.hash_count):
            index = mmh3.hash(url, seed)
            if seed == 0 or minv > self.matrix[seed, index % self.columns]:
                minv = self.matrix[seed, index % self.columns]
        return minv



