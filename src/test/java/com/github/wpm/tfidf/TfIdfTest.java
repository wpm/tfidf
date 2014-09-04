package com.github.wpm.tfidf;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class TfIdfTest {
    private List<Collection<String>> documents;

    private Set<String> document1Terms = ImmutableSet.of(
            "to", "be", "or", "not", "to", "be",
            "to be or", "be or not", "or not to", "not to be"
    );

    private Set<String> document2Terms = ImmutableSet.of(
            "or", "to", "jump",
            "or to jump"
    );

    @Before
    public void setUp() {
        documents = Lists.newArrayList(NgramTfIdf.ngramDocumentTerms(Lists.newArrayList(1, 3),
                Arrays.asList("to be or not to be", "or to jump")));
    }

    @Test
    public void tf() {
        Map<String, Double> tf;
        tf = TfIdf.tf(documents.get(0));
        assertEquals(document1Terms, tf.keySet());
        assertEquals((Double) 2.0, tf.get("to"));
        assertEquals((Double) 2.0, tf.get("be"));
        assertEquals((Double) 1.0, tf.get("or"));
        assertEquals((Double) 1.0, tf.get("not"));

        tf = TfIdf.tf(documents.get(1));
        assertEquals(document2Terms, tf.keySet());
        assertEquals((Double) 1.0, tf.get("or"));
        assertEquals((Double) 1.0, tf.get("to"));
        assertEquals((Double) 1.0, tf.get("jump"));
    }

    @Test
    public void idf() {
        Iterable<Map<String, Double>> tfs = TfIdf.tfs(documents);
        Map<String, Double> idf = TfIdf.idfFromTfs(tfs);
        Set<String> allTerms = new HashSet<>();
        allTerms.addAll(document1Terms);
        allTerms.addAll(document2Terms);
        assertEquals(allTerms, idf.keySet());
        assertEquals((Double) 1.0, idf.get("to"));
        assertEquals((Double) (1 + Math.log(3.0 / 2.0)), idf.get("be"));
        assertEquals((Double) 1.0, idf.get("or"));
        assertEquals((Double) (1 + Math.log(3.0 / 2.0)), idf.get("not"));
        assertEquals((Double) (1 + Math.log(3.0 / 2.0)), idf.get("jump"));
    }

    @Test
    public void tfIdf() {
        List<Map<String, Double>> tfs = Lists.newArrayList(TfIdf.tfs(documents));
        Map<String, Double> idf = TfIdf.idfFromTfs(tfs);
        Map<String, Double> tfIdf;

        tfIdf = TfIdf.tfIdf(tfs.get(0), idf);
        assertEquals(Sets.newHashSet(document1Terms), tfIdf.keySet());
        assertEquals((Double) 2.0, tfIdf.get("to"));
        assertEquals((Double) (2.0 * (1 + Math.log(3.0 / 2.0))), tfIdf.get("be"));
        assertEquals((Double) 1.0, tfIdf.get("or"));
        assertEquals((Double) (1 + Math.log(3.0 / 2.0)), tfIdf.get("not"));

        tfIdf = TfIdf.tfIdf(tfs.get(1), idf);
        assertEquals(Sets.newHashSet(document2Terms), tfIdf.keySet());
        assertEquals((Double) 1.0, tfIdf.get("or"));
        assertEquals((Double) 1.0, tfIdf.get("to"));
        assertEquals((Double) (1 + Math.log(3.0 / 2.0)), tfIdf.get("jump"));
    }

    @Test
    public void ngramDocumentTerms() {
        assertEquals(2, documents.size());

        Set<String> terms;
        terms = Sets.newHashSet(documents.get(0));
        assertEquals(document1Terms, terms);
        terms = Sets.newHashSet(documents.get(1));
        assertEquals(document2Terms, terms);
    }
}