# Lucene Workshop: Class Exercise (8 minutes)

Run every command yourself from the project folder. After each step, **look at the ranking and answer the question** before we discuss it.

**Run command:** `java -cp "lib/*" src/Workshop.java <query> [options]`

### The dataset (`data/docs.txt`)

| ID | Title | Words | Contains |
|---|---|---|---|
| D1 | Search engine basics | 9 | search ×1, engine ×1 |
| D2 | Study habits blog | 48 | search ×7 |
| D3 | Comparing tools | 11 | search**ing**, engine**s** |
| D4 | Inverted index | 14 | inverted, index |
| D5 | History of the web | 53 | search ×1, engine ×1 |
| D6 | Road trip | 11 | engine, search (about a car!) |
| D7 | Apache Lucene | 10 | index**ing** |
| D8 | BM25 ranking | 12 | (none of the above) |

---

### Step 0: What does an analyzer do?
```bash
java -cp "lib/*" src/Workshop.java --tokens "The Searching Engines are indexing!"
java -cp "lib/*" src/Workshop.java --tokens "The Searching Engines are indexing!" --analyzer english
```
❓ Which words disappeared? Which changed? What will this do to matching?

### Step 1: Your first BM25 ranking
```bash
java -cp "lib/*" src/Workshop.java "search engine"
```
❓ D1 and D5 both contain *search* and *engine* exactly once. Why is D1 ranked above D5?
❓ D6 is about a car. Why is it ranked #2?

### Step 2: Ask Lucene to explain the scores
```bash
java -cp "lib/*" src/Workshop.java "search engine" --explain 4
```
Fill in for the term **search**:

| | idf | freq | dl (length) | tf | score |
|---|---|---|---|---|---|
| D1 | | | | | |
| D5 | | | | | |

❓ Which part of the BM25 formula made the difference: IDF, frequency, or length?
❓ Why does *engine* have a higher IDF than *search*?

### Step 3: Term-frequency saturation (k1)
```bash
java -cp "lib/*" src/Workshop.java "search"
java -cp "lib/*" src/Workshop.java "search" --k1 3
java -cp "lib/*" src/Workshop.java "search" --k1 0
```
❓ D2 says "search" 7 times, D1 only once. Is D2's score 7× higher? What happens when k1 goes up? When k1 = 0?

### Step 4: Turn off length normalization (b)
```bash
java -cp "lib/*" src/Workshop.java "search" --b 0
```
❓ Look at D1, D5 and D6. What happened, and why?

### Step 5: Analyzers change what matches
```bash
java -cp "lib/*" src/Workshop.java "searching engines"
java -cp "lib/*" src/Workshop.java "searching engines" --analyzer english
```
❓ Why does the first query find only D3? Why does the second find 5 documents?
❓ Look at the "Query parsed:" line. What did Lucene actually search for?

### Step 6: Fix the "Road trip" problem
```bash
java -cp "lib/*" src/Workshop.java "\"search engine\""
```
❓ Where did D6 go? What does a phrase query need that BM25's bag-of-words doesn't use?

### Bonus (if you finish early)
```bash
java -cp "lib/*" src/Workshop.java "search -engine"
java -cp "lib/*" src/Workshop.java "inverted index" --analyzer english
```
❓ Why does D7 match the second query only with the English analyzer?

---

### BM25 in Lucene (cheat sheet)

score(D, Q) = Σ over query terms t:  **idf(t) × tf(t, D)**

- idf(t) = log(1 + (N − n + 0.5) / (n + 0.5)). Rare terms (small n) score higher.
- tf(t, D) = freq / (freq + k1 × (1 − b + b × dl / avgdl))
  - **k1** controls saturation: how fast extra repetitions stop helping.
  - **b** controls length normalization: how much long documents are penalized.

Lucene drops the constant (k1 + 1) factor from classic BM25. It doesn't change the ranking.
