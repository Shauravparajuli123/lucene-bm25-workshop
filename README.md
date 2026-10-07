# Apache Lucene Hands-On Workshop

**Presenters:** Suman KC & Shaurav Parajuli · Information Retrieval

Index 8 small documents with Apache Lucene, run queries, and see *exactly* why BM25 ranks one document above another.

| File | What it is |
|---|---|
| `src/Workshop.java` | The whole program: analyzer → index → query → BM25 ranking → explain |
| `data/docs.txt` | The dataset: 8 documents (`ID <tab> Title <tab> Body`) |
| `HANDOUT.md` | Step-by-step class exercise |
| `ANSWER_KEY.md` | Expected outputs and explanations (presenters) |
| `get-jars.sh` / `get-jars.ps1` | Download Lucene 10.5.0 jars into `lib/` |

---

## 1. Setup (about 5 minutes; do this BEFORE class)

### Option A: GitHub Codespaces (easiest, nothing to install)
1. Open the repo on GitHub → **Code** → **Codespaces** → **Create codespace**.
2. Wait for it to load. Java 21 and the Lucene jars install automatically.
3. Skip to **Step 2: Run**.

### Option B: Your own laptop

**1. Install Java 21 or newer** (Lucene 10 requires Java 21)

| OS | Command |
|---|---|
| macOS | `brew install openjdk@21` (or install Temurin 21 from adoptium.net) |
| Windows | Install **Temurin 21** from https://adoptium.net (tick "Set JAVA_HOME") |
| Ubuntu | `sudo apt install openjdk-21-jdk` |

Check it: `java -version` should show 21 or higher.

**2. Get the project**
```bash
git clone <REPO-URL>
cd lucene-workshop
```
(or download the ZIP and unzip it)

**3. Download Lucene** (5 jars, about 5 MB)

| OS | Command |
|---|---|
| macOS / Linux | `bash get-jars.sh` |
| Windows (PowerShell) | `powershell -ExecutionPolicy Bypass -File get-jars.ps1` |

No Maven or IDE needed. Java 21 can run a single `.java` file directly.

---

## 2. Run

Always run from the project folder:

```bash
java -cp "lib/*" src/Workshop.java "search engine"
```

Expected output:
```
Analyzer: standard | BM25 k1=1.20 b=0.75 | 8 docs indexed
Query parsed: body:search body:engine
Rank  ID   Score    Words  Title
1     D1   0.9728   9      Search engine basics
2     D6   0.9259   11     Road trip
3     D2   0.5192   48     Study habits blog
4     D5   0.4658   53     History of the web
```
If you see this, you're ready. A `WARNING ... VectorizationProvider` line is harmless.

### All options

| Option | Example | Effect |
|---|---|---|
| *(query)* | `"search engine"` | Lucene query syntax: words, `"phrases"`, `AND`, `OR`, `-exclude` |
| `--analyzer` | `--analyzer english` | `standard` (default) or `english` (stopwords + stemming) |
| `--explain N` | `--explain 2` | Prints the BM25 score breakdown for the top N results |
| `--k1` | `--k1 2.0` | BM25 term-frequency saturation (default 1.2) |
| `--b` | `--b 0` | BM25 length normalization (default 0.75; 0 = off) |
| `--tokens` | `--tokens "Searching Engines"` | Shows what the analyzer turns text into (no search) |

---

## 3. Optional: look inside the index with Luke

Luke is Lucene's GUI for browsing an index's terms and postings.
1. Download the **binary** release from https://lucene.apache.org/core/downloads.html and unzip it.
2. Run the workshop once (it creates the `index/` folder).
3. Launch `bin/luke.sh` (macOS/Linux) or `bin\luke.cmd` (Windows), then open the `index/` folder.
4. **Overview** tab → field `body` → top terms. **Documents** tab → term vectors and postings.

---

## Troubleshooting

| Problem | Fix |
|---|---|
| `error: class found on application class path` or `ClassNotFoundException: org.apache.lucene...` | You're not in the project folder, or `lib/` is empty. Re-run `get-jars` |
| `UnsupportedClassVersionError` / `release 21 not supported` | Java is older than 21. Install 21 and check with `java -version` |
| `NoSuchFileException: data/docs.txt` | Run from the project root, not from `src/` |
| Windows: `lib/*` not expanding | Keep the quotes: `-cp "lib/*"`. In cmd you can also use `-cp "lib\*"` |
| `curl` / download blocked | Download the 5 jars manually from https://repo1.maven.org/maven2/org/apache/lucene/ into `lib/` |
