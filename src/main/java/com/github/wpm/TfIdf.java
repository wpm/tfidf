package com.github.wpm;


import java.util.*;

/**
 * Term frequency-Inverse document frequency
 */
public class TfIdf {

    public enum TfType {NATURAL, LOGARITHM, BOOLEAN}

    public enum Normalization {NONE, COSINE}

    /**
     * Term frequency for a single document
     *
     * @param document bag of terms
     * @param type     natural or logarithmic
     * @param <TERM>   term type
     * @return map of terms to the number of times they appear in the document
     */
    public static <TERM> Map<TERM, Double> tf(Collection<TERM> document, TfType type) {
        Map<TERM, Double> tf = new HashMap<>();
        for (TERM term : document) {
            tf.put(term, tf.getOrDefault(term, 0.0) + 1);
        }
        if (type != TfType.NATURAL) {
            for (TERM term : tf.keySet()) {
                switch (type) {
                    case LOGARITHM:
                        tf.put(term, 1 + Math.log(tf.get(term)));
                        break;
                    case BOOLEAN:
                        tf.put(term, tf.get(term) == 0.0 ? 0.0 : 1.0);
                        break;
                }
            }
        }
        return tf;
    }

    public static <TERM> Map<TERM, Double> tf(Collection<TERM> document) {
        return tf(document, TfType.NATURAL);
    }

    /**
     * Term frequencies for a set of documents
     *
     * @param documents sequence of documents, each of which is a bag of terms
     * @param <TERM>    term type
     * @return sequence of map of terms to the number of times they appear in the documents
     */
    public static <TERM> Iterable<Map<TERM, Double>> tfs(Iterable<Collection<TERM>> documents, TfType type) {
        List<Map<TERM, Double>> tfs = new ArrayList<>();
        for (Collection<TERM> document : documents) {
            tfs.add(tf(document, type));
        }
        return tfs;
    }

    public static <TERM> Iterable<Map<TERM, Double>> tfs(Iterable<Collection<TERM>> documents) {
        return tfs(documents, TfType.NATURAL);
    }

    /**
     * Inverse document frequency for a set of documents
     *
     * @param documentVocabularies sets of terms which appear in the documents
     * @param smooth               smooth the counts by treating the document set as if it contained an additional
     *                             document with every term in the vocabulary
     * @param addOne               add one to idf values to prevent divide by zero errors in tf-idf
     * @param <TERM>               term type
     * @return map of terms to the number of documents they appear in, the minimum value is smoothed to 1
     */
    public static <TERM> Map<TERM, Double> idf(Iterable<Iterable<TERM>> documentVocabularies,
                                               boolean smooth, boolean addOne) {
        Map<TERM, Integer> df = new HashMap<>();
        int d = smooth ? 1 : 0;
        int a = addOne ? 1 : 0;
        int n = d;
        for (Iterable<TERM> documentVocabulary : documentVocabularies) {
            n += 1;
            for (TERM term : documentVocabulary) {
                df.put(term, df.getOrDefault(term, d) + 1);
            }
        }
        Map<TERM, Double> idf = new HashMap<>();
        for (Map.Entry<TERM, Integer> e : df.entrySet()) {
            TERM term = e.getKey();
            double f = e.getValue();
            idf.put(term, Math.log(n / f) + a);
        }
        return idf;
    }

    public static <TERM> Map<TERM, Double> idf(Iterable<Iterable<TERM>> documentVocabularies) {
        return idf(documentVocabularies, true, true);
    }

    /**
     * tf-idf for a document
     *
     * @param tf            term frequencies of the document
     * @param idf           inverse document frequency for a set of documents
     * @param normalization none or cosine
     * @param <TERM>        term type
     * @return map of terms to their tf-idf values
     */
    public static <TERM> Map<TERM, Double> tfIdf(Map<TERM, Double> tf, Map<TERM, Double> idf,
                                                 Normalization normalization) {
        Map<TERM, Double> tfIdf = new HashMap<>();
        for (TERM term : tf.keySet()) {
            tfIdf.put(term, tf.get(term) * idf.get(term));
        }
        if (normalization == Normalization.COSINE) {
            double n = 0.0;
            for (double x : tfIdf.values()) {
                n += x * x;
            }
            n = Math.sqrt(n);

            for (TERM term : tfIdf.keySet()) {
                tfIdf.put(term, tfIdf.get(term) / n);
            }
        }
        return tfIdf;
    }

    public static <TERM> Map<TERM, Double> tfIdf(Map<TERM, Double> tf, Map<TERM, Double> idf) {
        return tfIdf(tf, idf, Normalization.NONE);
    }

    /**
     * Utility to build inverse document frequencies from a set of term frequencies
     *
     * @param tfs    term frequencies for a set of documents
     * @param smooth smooth the counts by treating the document set as if it contained an additional
     *               document with every term in the vocabulary
     * @param addOne add one to idf values to prevent divide by zero errors in tf-idf
     * @param <TERM> term type
     * @return map of terms to their tf-idf values
     */
    public static <TERM> Map<TERM, Double> idfFromTfs(Iterable<Map<TERM, Double>> tfs, boolean smooth, boolean addOne) {
        return idf(new KeySetIterable<>(tfs), smooth, addOne);
    }

    public static <TERM> Map<TERM, Double> idfFromTfs(Iterable<Map<TERM, Double>> tfs) {
        return idfFromTfs(tfs, true, true);
    }

    /**
     * Iterator over the key sets of a set of maps.
     *
     * @param <KEY>   map key type
     * @param <VALUE> map value type
     */
    static private class KeySetIterable<KEY, VALUE> implements Iterable<Iterable<KEY>> {
        final private Iterator<Map<KEY, VALUE>> maps;

        public KeySetIterable(Iterable<Map<KEY, VALUE>> maps) {
            this.maps = maps.iterator();
        }

        @Override
        public Iterator<Iterable<KEY>> iterator() {
            return new Iterator<Iterable<KEY>>() {
                @Override
                public boolean hasNext() {
                    return maps.hasNext();
                }

                @Override
                public Iterable<KEY> next() {
                    return maps.next().keySet();
                }
            };
        }
    }
}
