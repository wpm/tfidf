Term Frequency-Inverse Document Frequency
=========================================

This package provides utilities for calculating tf-idf for a set of documents.
A document is a bag of terms, where the definition of term is left to the caller.

The example program *NgramTfIdf* calculates tf-idf of n-gram frequencies.
It takes a single file as an argument and treats each line of that file as a separate document, calculating tf-idf for
n-gram terms.
