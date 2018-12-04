import matplotlib.pyplot as plt

def pieChart(cats):
    labels = []
    percentages = []
    cat_sum = sum(cats.values())
    for k, v in cats.items():
        labels.append(k)
        percentages.append(v/cat_sum)
    fig1, ax1 = plt.subplots()
    ax1.pie(percentages, labels=labels, autopct='%1.1f%%',
            shadow=True, startangle=90)
    ax1.axis('equal')  # Equal aspect ratio ensures that pie is drawn as a circle.
    plt.show()
    return

def columnChart(heavy_hitters, title, amount):
    words = []
    counts = []
    HHs = heavy_hitters[title]
    for i in range(0, len(HHs[:amount*2]), 2):
        words.append(HHs[i])
        counts.append(float(HHs[i+1]))
    fig, axs = plt.subplots()
    fig.suptitle(title, y=1.0)
    axs.bar(words, counts)
    locs, labels = plt.xticks()
    plt.setp(labels, rotation=90)
    plt.show()
    return

def uniqueWords(heavy_hitters, cat, top=10):
    unique_words = set([heavy_hitters[cat][i] for i in range(0, len(heavy_hitters[cat][:top*2]), 2)])
    for k, _ in heavy_hitters.items():
        if k == cat:
            continue
        unique_words = unique_words - set([heavy_hitters[k][i] for i in range(0, len(heavy_hitters[k][:top*2]), 2)])
    return unique_words

if __name__ == "__main__":
    result = "logs_from_29-11_donotcancel.txt"
    HHs = {}
    Cats = {}
    with open(result, 'r', encoding="UTF-8") as f:
        for line in f:
            if 'Counter' in line:
                line = line.replace("(", "").replace("{", "").replace(")", "").replace("}", "").replace("Counter", "")\
                           .replace(":", "").replace(",", "").replace("\'", "").replace("\n", "").split(" ")
                HHs[line[0]] = line[1:]
            else:
                line = line.replace(" mapped to", "").replace("\n", "").split(" ")
                Cats[line[1]] = int(line[0])
    #pieChart(Cats)
    #columnChart(HHs, 'Religion', 10)
    print(uniqueWords(HHs, 'Politics'))