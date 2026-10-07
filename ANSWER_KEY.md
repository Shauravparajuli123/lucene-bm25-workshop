# Answer Key (presenters only)

All numbers verified with Lucene 10.5.0. N = 8 docs, avgdl = 21.125 tokens (StandardAnalyzer).

### Step 0: Analyzer
| Analyzer | Tokens |
|---|---|
| standard | `[the, searching, engines, are, indexing]` (lowercased only) |
| english | `[search, engin, index]` (stopwords *the/are* removed, Porter stemming) |

Point: the index stores **terms**, not words. The same analyzer runs on documents and queries.

### Step 1: `"search engine"`
| Rank | Doc | Score |
|---|---|---|
| 1 | D1 | 0.9728 |
| 2 | D6 | 0.9259 |
| 3 | D2 | 0.5192 |
| 4 | D5 | 0.4658 |

- **D1 > D5:** same terms, same frequency, but D1 has 9 tokens vs D5's 52. Length normalization (b = 0.75) penalizes D5.
- **D6 at #2:** BM25 is **bag-of-words**. D6 contains both terms and is short, so BM25 can't tell it's about a car. Lexical match ≠ relevance (fixed in Step 6).
- **D2 at #3:** it has "search" 7 times but no "engine". Matching both terms beats repeating one.
- QueryParser default is **OR**, so 4 docs match.

### Step 2: explain, term "search"
| | idf | freq | dl | tf | score |
|---|---|---|---|---|---|
| D1 | 0.6931 | 1 | 9 | 0.5940 | 0.4117 |
| D5 | 0.6931 | 1 | 52 | 0.2845 | 0.1972 |

- IDF is identical (same term, n = 4). **Only dl differs**, so length normalization explains the whole gap.
- engine: n = 3 → idf 0.9445 > search: n = 4 → idf 0.6931. Rarer terms carry more weight.
- `dl ... (approximate)`: Lucene stores lengths in a compressed 1-byte "norm", so long-doc lengths are rounded.

### Step 3: `"search"`, varying k1
| Doc (freq) | k1 = 1.2 | k1 = 3 | k1 = 0 |
|---|---|---|---|
| D2 (7) | **0.5192** | **0.3772** | 0.6931 |
| D1 (1) | 0.4117 | 0.2559 | 0.6931 |
| D6 (1) | 0.3919 | 0.2372 | 0.6931 |
| D5 (1) | 0.1972 | 0.0951 | 0.6931 |

- 7× the occurrences gives only **1.26×** the score: **saturation**. D2 is also long, which pulls it down.
- k1 = 3 → D2/D1 ratio rises to **1.47×**: repetition matters more.
- k1 = 0 → tf = 1 for every match, so all docs tie on IDF alone (binary/Boolean-style matching).
- Scores shrink as k1 grows because Lucene omits the (k1 + 1) factor. Compare ratios, not raw scores.

### Step 4: `"search" --b 0`
D2 0.5917; D1 = D5 = D6 = **0.3151** (tie). With b = 0, document length is ignored, so all single-occurrence docs score the same. The long blog post D5 is no longer penalized. Tie order is just internal doc ID.

### Step 5: `"searching engines"`
- standard: parsed `body:searching body:engines`. Only D3 has those exact surface forms. Score is high (2.03) because the terms are **rare** (n = 1 → high IDF).
- english: parsed `body:search body:engin`. Stemming makes searching→search and engines→engin, so it matches D1, D6, D3, D2, D5. **Recall goes up.**
- Takeaway: analyzer choice is a precision/recall trade-off, and it must be the same at index and query time.

### Step 6: `"\"search engine\""`
Only D1 (0.9728) and D5 (0.4658) match. A **PhraseQuery** uses the **positions** stored in the inverted index ("search" immediately followed by "engine"). D6 ("engine ... search") fails. BM25 still ranks the phrase matches (D1 > D5, length again).

### Bonus
- `search -engine` → only D2 (MUST_NOT clause; BooleanQuery).
- `inverted index --analyzer english` → D4 1.75, D7 0.75. D7's "indexing" stems to "index". With standard, only D4 matches (1.89).

### Discussion prompts for the end
1. Is the #2 result for "search engine" *relevant*? What would fix it beyond phrases (proximity, semantic/vector search)?
2. Which would you choose for a code-search engine: stemming or no stemming? Why?
3. Elasticsearch and OpenSearch use this exact BM25Similarity under the hood. What do they add on top?
