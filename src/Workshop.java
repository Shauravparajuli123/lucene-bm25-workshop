import java.nio.file.*;
import java.util.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;

/**
 * Apache Lucene hands-on workshop: index 8 documents, search them, inspect BM25.
 *
 * Usage (from the project root):
 *   java -cp "lib/*" src/Workshop.java "search engine"
 *   java -cp "lib/*" src/Workshop.java "search engine" --explain 2
 *   java -cp "lib/*" src/Workshop.java "searching engines" --analyzer english
 *   java -cp "lib/*" src/Workshop.java "search" --k1 1.2 --b 0.0
 *   java -cp "lib/*" src/Workshop.java --tokens "The Searching Engines!" --analyzer english
 */
public class Workshop {

    static final Path DATA = Paths.get("data/docs.txt");
    static final Path INDEX_DIR = Paths.get("index");

    public static void main(String[] args) throws Exception {
        // ---------- 0. Parse command-line options ----------
        String query = null, tokensText = null, analyzerName = "standard";
        float k1 = 1.2f, b = 0.75f;   // Lucene's BM25 defaults
        int explainTop = 0, topK = 8;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--analyzer" -> analyzerName = args[++i];
                case "--k1"       -> k1 = Float.parseFloat(args[++i]);
                case "--b"        -> b = Float.parseFloat(args[++i]);
                case "--explain"  -> explainTop = Integer.parseInt(args[++i]);
                case "--top"      -> topK = Integer.parseInt(args[++i]);
                case "--tokens"   -> tokensText = args[++i];
                default           -> query = args[i];
            }
        }

        // ---------- 1. Choose an analyzer ----------
        // StandardAnalyzer: split on word boundaries + lowercase.
        // EnglishAnalyzer: the above + stopword removal + Porter stemming (searching -> search).
        Analyzer analyzer = analyzerName.equalsIgnoreCase("english")
                ? new EnglishAnalyzer()
                : new StandardAnalyzer();

        if (tokensText != null) {          // just show what the analyzer produces
            printTokens(analyzer, tokensText, analyzerName);
            return;
        }
        if (query == null) {
            System.out.println("Give a query, e.g.  java -cp \"lib/*\" src/Workshop.java \"search engine\"");
            return;
        }

        // ---------- 2. Ranking function: BM25 ----------
        BM25Similarity bm25 = new BM25Similarity(k1, b);

        // ---------- 3. Build the inverted index ----------
        try (FSDirectory dir = FSDirectory.open(INDEX_DIR)) {
            IndexWriterConfig cfg = new IndexWriterConfig(analyzer)
                    .setSimilarity(bm25)
                    .setOpenMode(IndexWriterConfig.OpenMode.CREATE); // rebuild every run
            try (IndexWriter writer = new IndexWriter(dir, cfg)) {
                for (String line : Files.readAllLines(DATA)) {
                    if (line.isBlank()) continue;
                    String[] f = line.split("\t");
                    Document doc = new Document();
                    doc.add(new StringField("id", f[0], Field.Store.YES)); // exact, not analyzed
                    doc.add(new StoredField("title", f[1]));               // stored only, not searched
                    doc.add(new TextField("body", f[2], Field.Store.YES)); // analyzed + indexed
                    writer.addDocument(doc);
                }
            }

            // ---------- 4. Parse the query and search ----------
            try (DirectoryReader reader = DirectoryReader.open(dir)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                searcher.setSimilarity(bm25);  // must match the similarity used at index time

                Query q = new QueryParser("body", analyzer).parse(query);
                TopDocs hits = searcher.search(q, topK);
                StoredFields stored = searcher.storedFields();

                System.out.printf("%nAnalyzer: %s | BM25 k1=%.2f b=%.2f | %d docs indexed%n",
                        analyzerName, k1, b, reader.numDocs());
                System.out.println("Query typed : " + query);
                System.out.println("Query parsed: " + q + "   <- what Lucene actually searches");
                System.out.println("Matches     : " + hits.totalHits.value());
                System.out.println("-------------------------------------------------------------");
                System.out.printf("%-5s %-4s %-8s %-6s %s%n", "Rank", "ID", "Score", "Words", "Title");
                int rank = 1;
                for (ScoreDoc sd : hits.scoreDocs) {
                    Document d = stored.document(sd.doc);
                    int words = d.get("body").split("\\s+").length;
                    System.out.printf("%-5d %-4s %-8.4f %-6d %s%n",
                            rank++, d.get("id"), sd.score, words, d.get("title"));
                }
                if (hits.scoreDocs.length == 0) System.out.println("(no documents matched)");

                // ---------- 5. Why did it rank there? ----------
                for (int i = 0; i < Math.min(explainTop, hits.scoreDocs.length); i++) {
                    ScoreDoc sd = hits.scoreDocs[i];
                    System.out.println("\n========== EXPLAIN rank " + (i + 1) + ": "
                            + stored.document(sd.doc).get("id") + " ==========");
                    System.out.println(searcher.explain(q, sd.doc));
                }
            }
        }
    }

    static void printTokens(Analyzer analyzer, String text, String name) throws Exception {
        System.out.println("\nAnalyzer: " + name);
        System.out.println("Input   : " + text);
        List<String> out = new ArrayList<>();
        try (TokenStream ts = analyzer.tokenStream("body", text)) {
            CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
            ts.reset();
            while (ts.incrementToken()) out.add(term.toString());
            ts.end();
        }
        System.out.println("Tokens  : " + out + "   <- these terms go into the inverted index");
    }
}
