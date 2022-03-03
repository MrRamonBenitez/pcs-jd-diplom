import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static java.util.Collections.sort;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

public class BooleanSearchEngine implements SearchEngine {
    Map<String, List<Map<String, Long>>> pdfIndex;

    public BooleanSearchEngine(File pdfsDir) throws IOException {

        this.pdfIndex = indexing(pdfsDir);
    }

        private Map<String, List<Map<String, Long>>> indexing(File dir) throws IOException {
            Map<String, List<Map<String, Long>>> pdfFiles = new HashMap<>();
            List<Map<String, Long>> pageList;

            if (dir.isDirectory()) {

                try {
                    for (File item : dir.listFiles()) {

                        try (PdfDocument doc = new PdfDocument(new PdfReader(item))) {
                            pageList = new ArrayList<>();
                            for (int i = 0; i < doc.getNumberOfPages(); i++) {

                                PdfPage page = doc.getPage(i + 1);

                                String textFromPage = PdfTextExtractor.getTextFromPage(page);

                                String[] words = textFromPage.split("\\P{IsAlphabetic}+");

                                Map<String, Long> wordСount = Arrays.stream(words)                  //мапа с парой
                                    .map(String::toLowerCase)                                   // слово = кол-во повт.
                                    .collect(groupingBy(Function.identity(), counting()));

                                pageList.add(i, wordСount);
                            }
                        }

                        pdfFiles.put(item.getName(), pageList);
                    }

                } catch (NullPointerException ex) {
                    System.out.println(ex.getMessage());
                }

                return pdfFiles;
            }

            return null;
        }

    @Override
    public List<PageEntry> search(String word) {
        String pdfName;
        int page;
        int count;
        List<PageEntry> searchResultList = new ArrayList<>();

        for (String item : pdfIndex.keySet()) {

            if (pdfIndex.containsKey(item)) {

                List<Map<String, Long>> doc = pdfIndex.get(item);
                pdfName = item;

                for (int i = 0; i < doc.size(); i++) {

                    count = 0;

                    if (doc.get(i).containsKey(word)) {
                        long countLong = doc.get(i).get(word);
                        count = count + (int) countLong;
                    }

                    page = i + 1;

                    if (count != 0) {
                        searchResultList.add(new PageEntry(pdfName, page, count));
                    }
                }
            }
        }

        sort(searchResultList);
        return searchResultList;
    }
}

