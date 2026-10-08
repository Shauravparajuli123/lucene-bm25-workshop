# Apache Lucene Hands-On Workshop

**Presenters:** Suman KC & Shaurav Parajuli · Information Retrieval

In this workshop you will use **Apache Lucene** to index 8 small documents, run searches, and see **why BM25 ranks one document above another**. You will run every command yourself.

> ⏱️ **Please complete Part 1 (Setup) before the workshop.** It takes about 5 minutes.

---

## Part 1: Setup (choose ONE option)

### ✅ Option A: GitHub Codespaces (recommended; nothing to install)

| Step | What to do |
|---|---|
| 1 | Log in to GitHub and open this repository |
| 2 | Click the green **`<> Code`** button → **Codespaces** tab → **Create codespace on main** |
| 3 | Wait 1–3 minutes while it installs Java and Lucene automatically |
| 4 | Click inside the **terminal** at the bottom of the screen (no terminal? press **Ctrl + `**) |
| 5 | Go to **Part 2** |

Tip: in the Codespaces terminal, paste with **Ctrl + Shift + V**.

### Option B: Your own laptop

**1. Install Java 21 or newer** (Lucene 10 requires Java 21)

| OS | How |
|---|---|
| Windows | Open **Command Prompt** and run `winget install EclipseAdoptium.Temurin.21.JDK`, or download Temurin 21 from https://adoptium.net (tick **"Set JAVA_HOME"**) |
| macOS | `brew install openjdk@21` (or install Temurin 21 from https://adoptium.net) |
| Linux | `sudo apt install openjdk-21-jdk` |

Close and reopen the terminal, then check: `java -version` should show **21** or higher.

**2. Download this repository**

```
git clone https://github.com/Shauravparajuli123/lucene-bm25-workshop.git
cd lucene-bm25-workshop
```
No Git? Click **`<> Code` → Download ZIP**, unzip it, and open a terminal inside the folder.

**3. Download Lucene** (5 small files)

| OS | Command |
|---|---|
| Windows | `powershell -ExecutionPolicy Bypass -File get-jars.ps1` |
| macOS / Linux | `bash get-jars.sh` |

> ⚠️ **Windows users:** use **Command Prompt (cmd)**, not PowerShell, for all workshop commands. Step 6 does not work in PowerShell.

---

## Part 2: Check that it works

Run from inside the project folder:

```
java -cp "lib/*" src/Workshop.java "search engine"
```

You should see:

```
Rank  ID   Score    Words  Title
1     D1   0.9728   9      Search engine basics
2     D6   0.9259   11     Road trip
3     D2   0.5192   48     Study habits blog
4     D5   0.4658   53     History of the web
```

✅ If you see this, you are ready for the workshop.
ℹ️ Lines starting with `WARNING:` are normal Java notices. Ignore them.

---

## Part 3: The workshop exercise

### How a command is built

```
java -cp "lib/*" src/Workshop.java "search engine" --explain 2
```

| Part | Meaning |
|---|---|
| `java` | Run Java |
| `-cp "lib/*"` | Load the Lucene library from the `lib` folder |
| `src/Workshop.java` | Our program: it builds the index and searches it |
| `"search engine"` | Your query |
| `--explain 2` | An option (see the table at the end) |

### The 8 documents (`data/docs.txt`)

| ID | Title | Words | Contains |
|---|---|---|---|
| D1 | Search engine basics | 9 | search ×1, engine ×1 |
| D2 | Study habits blog | 48 | search ×7 |
| D3 | Comparing tools | 11 | search**ing**, engine**s** |
| D4 | Inverted index | 14 | inverted, index |
| D5 | History of the web | 53 | search ×1, engine ×1 |
| D6 | Road trip | 11 | engine, search (a story about a car!) |
| D7 | Apache Lucene | 10 | index**ing** |
| D8 | BM25 ranking | 12 | none of the above |

Run each step, **look at the results, and think about the question.** We will discuss each one together.

### Step 0: What does an analyzer do?
```
java -cp "lib/*" src/Workshop.java --tokens "The Searching Engines are indexing!"
java -cp "lib/*" src/Workshop.java --tokens "The Searching Engines are indexing!" --analyzer english
```
❓ Which words disappeared? Which words changed?

### Step 1: Your first BM25 ranking
```
java -cp "lib/*" src/Workshop.java "search engine"
```
❓ D1 and D5 both contain *search* and *engine* exactly once. Why is D1 ranked higher?
❓ D6 is about a car. Why is it ranked #2?

### Step 2: Ask Lucene to explain the scores
```
java -cp "lib/*" src/Workshop.java "search engine" --explain 4
```
❓ Compare D1 and D5 for the term *search*: which part differs: `idf`, `freq`, or `dl` (length)?
❓ Why does *engine* get a higher `idf` than *search*?

### Step 3: Does repeating a word help?
```
java -cp "lib/*" src/Workshop.java "search"
java -cp "lib/*" src/Workshop.java "search" --k1 3
java -cp "lib/*" src/Workshop.java "search" --k1 0
```
❓ D2 says "search" 7 times and D1 only once. Is D2's score 7× higher? What changes when k1 goes up, or to 0?

### Step 4: Ignore document length
```
java -cp "lib/*" src/Workshop.java "search" --b 0
```
❓ Look at D1, D5 and D6. What happened, and why?

### Step 5: Analyzers change what matches
```
java -cp "lib/*" src/Workshop.java "searching engines"
java -cp "lib/*" src/Workshop.java "searching engines" --analyzer english
```
❓ Why does the first query find only D3, but the second finds 5 documents?
❓ Look at the `Query parsed:` line. What did Lucene actually search for?

### Step 6: Fix the "Road trip" problem
```
java -cp "lib/*" src/Workshop.java "\"search engine\""
```
❓ Where did D6 go? What does a phrase query check that a normal query doesn't?

### Bonus (if you finish early)
```
java -cp "lib/*" src/Workshop.java "search -engine"
java -cp "lib/*" src/Workshop.java "inverted index" --analyzer english
```
❓ What does `-engine` do? Why does D7 appear only with the English analyzer?

---

## All options

| Option | Example | Effect |
|---|---|---|
| *(query)* | `"search engine"` | Words, `"exact phrases"`, `AND`, `OR`, `-exclude` |
| `--analyzer` | `--analyzer english` | `standard` (default) or `english` (removes stopwords + stemming) |
| `--explain N` | `--explain 2` | Shows the BM25 calculation for the top N results |
| `--k1` | `--k1 3` | BM25 saturation parameter (default 1.2) |
| `--b` | `--b 0` | BM25 length normalization (default 0.75; 0 = off) |
| `--tokens` | `--tokens "some text"` | Shows the analyzer's output only (no search) |

Tip: press **↑** to repeat the last command, then edit it.

## BM25 cheat sheet

```
score(D, Q) = Σ  idf(t) × tf(t, D)          (sum over the query terms)

idf(t)      = ln(1 + (N − n + 0.5) / (n + 0.5))
tf(t, D)    = freq / (freq + k1 × (1 − b + b × dl / avgdl))
```

| Symbol | Meaning |
|---|---|
| N | Number of documents (8) |
| n | Documents containing the term (rare term → higher idf) |
| freq | Times the term appears in the document |
| dl / avgdl | Document length / average length |
| k1 | How fast repeated words stop adding score (1.2) |
| b | How much long documents are penalized (0.75) |

---

## Troubleshooting

| Problem | Fix |
|---|---|
| `java: command not found` / `'java' is not recognized` | Install Java 21, then **close and reopen** the terminal |
| `UnsupportedClassVersionError` or `release 21 not supported` | Your Java is too old. Install Java 21+ |
| `ClassNotFoundException: org.apache.lucene...` | The `lib` folder is empty. Run `get-jars` (Part 1, Option B, step 3) |
| `NoSuchFileException: data/docs.txt` | You are in the wrong folder. Go into `lucene-bm25-workshop` |
| Step 6 still shows D6 (Windows) | You are in PowerShell. Type `cmd`, press Enter, and run it again |
| Codespaces: `lib` is empty | Run `bash get-jars.sh` |
| Paste doesn't work in Codespaces | Use **Ctrl + Shift + V** or right-click → Paste |

**Using Codespaces?** When you're done, stop it to save your free hours: https://github.com/codespaces → **⋯** → **Stop codespace**.

## Optional: look inside the index with Luke

Luke is Lucene's visual tool for browsing an index. It works on your own laptop only.
1. Download the **binary** release from https://lucene.apache.org/core/downloads.html and unzip it.
2. Run any workshop command once (this creates the `index` folder).
3. Start `bin/luke.sh` (macOS/Linux) or `bin\luke.cmd` (Windows) and open the `index` folder.
