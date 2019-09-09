# SearchAndCrypt

A library to search through encrypted e-mails, written in Java.
This library is distributed with the GNU general public license version 3,
see the details in the file named "LICENSE".


## Purpose of this library ##

The main goal of this library is to enable searching functionalities inside a
set of encrypted e-mails, stored on a distant server. To this end, we rely on
the management of an inverted index which stores, for each word which is in the
e-mails, the set of e-mails which contain this word. This index is then stored,
encrypted, on the server.

Whenever an e-mail is added (the user receives a new e-mail or writes a new
e-mail) or is removed (the user deletes an old e-mail), the index has to be
updated.

Whenever the user wants to perform a search in their e-mails, they type some
words, and the library will show them the set of documents that contain all the
words they have typed.

The current state of this library gives examples of use of this library in a
non-encrypted, local, environment. It also gives a general A.P.I. that can be
used to perform these functionalities in an encrypted and client-server
environment.


## Run ##

The java files have been compiled to a .jar executable file located in:

    dist/SearchAndCrypt.jar

To run this executable file, just go in the root folder of this library (the
one in which this README.md file is located) and execute:

    launch.sh

It is mandatory to test the library from this folder, as the datasets are
accessed through the relative path directory datasets/ --- it would be also
possible to launch the test examples by copying the datasets folder in the
folder in which we want to perform the tests.


## Build ##

The library has been written with the Netbeans I.D.E., and all the files
associated to the Netbeans project are contained in this repository. The
easiest method is thus to open this Netbeans project and build it, if
necessary.


## Content of this repository ##

**build:** the files generated when building this library.

**datasets:** contains 5 sets of documents that can be used to test the
library:

* Bible: the King James' Bible [[2]](#note2) --- which is a dataset of choice
because it has been used a lot of times in previous works. This dataset
contains 31102 verses, and is split into different document at run-time.

* allen-p and dasovich-j: two e-mail boxes taken from the Enron corpus of
e-mails [[9]](#note9), which contains professional e-mail boxes of 150
persons. We cleaned the original folders by removing the e-mails which were
there multiple times, as suggested in the article. The allen-p dataset contains
1410 e-mails and the dasovich-j dataset contains 15748 e-mails.

* Fables: the Fables written by Jean de La Fontaine [[1]](#note1). This folder
contains 240 files, each containing a different fable.

* Hugo: 10 books written by Victor Hugo. This dataset is split at run-time into
documents of 30 lines each, resulting into a dataset of 6000 documents.

**dist:** the .jar executable of this library, build with Netbeans, also
containing the libraries on which this one depends.

**lib:** the libraries on which this one depends.

**nbproject:** Netbeans project files.

**src:** the source files of this library.


## The inverted index: a simple example ##

Let us start with a simple example, taken from "Managing Gigabytes"
[[3]](#note3). For now, we will omit the technical details which are part of
the representation of an e-mail, and will just consider a set of documents to
index. [Table 1](#table1) shows a set of 6 documents that we want to index.
[Table 2](#table2) shows the inverted index of this set of documents (sorted
alphabetically, in ascending order). The word "cold" is part of 2 documents:
document number 1, and document number 4 ; the word "days" is part of 2
documents: document number 3, and document number 6 ; etc.

<a name="table1">Table 1</a>: Example text; each line is one document.

| Document |                    Text                  |
|---------:|------------------------------------------|
|     1    | Pease porridge hot, pease porridge cold, |
|     2    | Pease porridge in the pot,               |
|     3    | Nine days old.                           |
|     4    | Some like it hot, some like it cold,     |
|     5    | Some like it in the pot,                 |
|     6    | Nine days old.                           |


<a name="table2">Table 2</a>: Inverted file for text of [Table 1](#table1).

| Number |   Term   | Documents |
|-------:|----------|:---------:|
|    1   | cold     | {1, 4}    |
|    2   | days     | {3, 6}    |
|    3   | hot      | {1, 4}    |
|    4   | in       | {2, 5}    |
|    5   | it       | {4, 5}    |
|    6   | like     | {4, 5}    |
|    7   | nine     | {3, 6}    |
|    8   | old      | {3, 6}    |
|    9   | pease    | {1, 2}    |
|   10   | porridge | {1, 2}    |
|   11   | pot      | {2, 5}    |
|   12   | some     | {4, 5}    |
|   13   | the      | {2, 5}    |

If the user wants to retrieve the documents which contain the word "old", it
is thus straightforward: the index directly tells that those are documents
number 3 and 6.

Now, what happens if the user wants to retrieve documents which contains
multiple words? The library will get the sets of documents which contain each
of those words, and intersect them. For example, if the user wants to retrieve
the documents which contain both "some" and "the", the index tells that
those documents are in the intersection between {4, 5} and {2, 5} which
is the set {5}. Only the document number 5 contains those 2 words.
If the user wants to retrieve the documents which contain both "cold"
and "days", the index tells us that those documents are the intersection
between {1, 4} and {3, 6} which is the empty set. No document
contains those 2 words.


## The dependencies ##

* the Apache Commons Primitives [[15]](#note15) library for the ArrayIntList
Java class. This class uses *32 x N* bits of memory for a list of *N* integers
instead of *128 x N* bits when using ArrayList<Integer>. Since those lists are
the most memory-consuming of our library, gaining a factor of 4 is significant.
However, the gains are only useful if one wants to index a set of documents the
fastest way possible --- by setting nbMailsBeforeSave (in MimeParse.java) to a
maximum possible value, the whole index will always be in main memory, instead
of being output in files from time to time. When setting nbMailsBeforeSave to a
more robust number (that allows to continue a non-finished indexing instead of
having to rebuild it from scratch), one can then change all the ArrayIntList to
ArrayList<Integer> without any memory concern.
N.B.: The ArrayIntList Java class can be easily rewritten if needed. It is thus
possible to avoid this dependency.

* the Apache JAMES Mime4j [[16]](#note16) library for the parsing of e-mails
in the MIME format.
N.B.: Parsing MIME messages is absolutely needed for the library. It would take
way too much time to reimplement a MIME parser, but parsers other than the
Apache JAMES Mime4j exist, *e.g.*, JavaMail
(https://javaee.github.io/javamail/).

* jsoup [[17]](#note17) library for the parsing of HTML portions of e-mails.
N.B.: parsing HTML is not mandatory, it just allows to avoid indexing useless
HTML markups. Because we only use the function text() which extracts the text
from a HTML document, this function can be rewritten if needed.

* the Snowball library [[18]](#note18) for stemming.
N.B.: Stemming is more than welcome, in the sense that it is practically
impossible for a user to know whether they have to search for, *e.g.*,
"computer" or "computers", when they perform a textual search. Getting rid of a
stemmer would increase a little bit the size of the index, but would primarily
render the searches less efficient for the user. The Porter algorithm for
stemming is also used by Apache Lucene, but it is possible to use other stemming
algorithms, *e.g.*, the one by J. Savoy
(http://members.unine.ch/jacques.savoy/clef/) or the one of Paice/Husk
(http://alx2002.free.fr/utilitarism/stemmer/stemmer_fr.html).


## Building the inverted index: choices made in this library ##

### Why an inverted index? ###

We choose to use an inverted index to index the e-mails. Other data structures
exist that allow more search features but that are more memory-consuming,
*e.g.*, the suffix trees or suffix arrays.

However, the bottleneck here is the memory, and not the computing time: because
the index is stored encrypted on the server, we have to download it (or upload
it) every time we need to update it or we need to perform a search. We will
thus use an inverted index: we will see that we can do much better than 30%
(the memory needed by suffix arrays) of the indexed text. Things would be
different if:

* the index was not stored encrypted (then, we could ask the server to perform
searches for us and would just need to send via the network the set of answers).
Encryption is mandatory to ensure privacy.

* the index could be stored locally on the client side (then, we could perform
all the updates and searches without having to worry about network bandwidth).
Take into consideration that the webmail engine can be used throughout
different devices. A possible solution to partially overcome the network
bandwidth issue while still enabling different devices is to store the index in
two parts: the full index at a given timestamp (still heavy to download and/or
upload) + a dynamic index which stores the differences from the full index, at
present time. The full index is then updated periodically (*e.g.*, once per
week), and thus can be downloaded just once per device per week ; only the
dynamic index will be downloaded and uploaded every time we modify it. This is
a viable solution if we use a mobile application, and if we use a webmail with
HTML5.

### How can we compress this inverted index? ###

A first point of comparison for the size of the inverted index is the size
claimed by Lucene: "index size roughly 20-30% the size of text indexed"
(https://lucene.apache.org/core/).

However, a typical e-mail user is expected to have a lot of e-mails, and in
that case, handling an index which is 20% the size of the e-mails is too much.
To do better, we have to look at the state-of-the-art concerning the
compression of the index. The index stores, for each word, the list of e-mails
IDs which contain this word --- the so-called inverted list. How can we store
those inverted lists in the most compact possible way?

Different approaches tackle this problem. We will first see how to reduce the
size of the index by reducing the number of words that appear in the index, and
will then see how to compress the inverted lists.

* A first approach is to normalize the words. A usual way to do this in
English-only texts is to convert all uppercase letters to their lowercase
equivalents. This transformation is known as case-folding, and is useful
because it will reduce the index size (if a document contains both "The"
and "the", the index now contains only one entry instead of two) while
improving most of the searching facilities (we do not want to search "Cat"
or "cat" to find all e-mails where we discussed about cats). The only downsize
of this method is that, sometimes, we would have liked to know if the word was
in uppercase or not (*e.g.*, to distinguish "the Bell prize" from "the bell
rang", or to distinguish "a MIME message" from "a mime artist", etc.). In our
library, we also remove all accents from letters ("félicité" becomes
"felicite"), and split the Unicode characters which contain two letters into
two ASCII letters ("tous mes vœux" becomes "tous mes voeux"). This is useful
for the same reason than before, and because accents and special characters are
frequently forgotten in e-mails.

* A second approach is to perform stemming. This transformation reduces each
word to its morphological root --- *i.e.*, it removes all suffixes and/or
modifiers. For example, compression, compressed, and compressor all have the
word compress as their common root.

* A last approach is to remove frequent words from the index, to save space.
However, choosing the list of stop-words is hard, and the gains are not that
good, because frequent words have an inverted list which can be greatly
compressed.

The previous methods all reduced the size of the index by authorizing a little
loss in the indexed terms. We will now see methods that compress without losing
any information. The main idea is the following: instead of storing all the
document IDs of an inverted list, we store the *differences* (or *gaps*)
between two consecutive IDs in that list --- if the inverted list is sorted, it
is a list of positive integers.

For example, the sorted sequence of document numbers

    7, 18, 19, 22, 23, 25, 63, ...

can be represented by gaps

    7, 11, 1, 3, 1, 2, 38, ...

Multiple ways to compress this list of gaps have been proposed.
[Table 3](#table3) [[3]](#note3) shows the compression which can be
obtained via most of those methods. In this table, four datasets have been
used: the King James' Bible, a bibliographic dataset (GNUbib), a collection of
law documents (Comact: the Commonwealth Acts of Austria), and a collection of
documents frequently used in the Information Retrieval community (TREC: Text
REtrieval Conference). In this table, the word *pointer* means an e-mail ID in
an inverted list. The less bits a pointer takes in memory, the more efficient
is the compression method.

<a name="table3">Table 3</a>: Compression of inverted files in bits per pointer.

| Method                                           | Bible  | GNUbib | Comact | TREC    |
|--------------------------------------------------|-------:|-------:|-------:|--------:|
| *Global methods*                                 |        |        |        |         |
| Unary                                            | 262.00 | 909.00 | 487.00 | 1918.00 |
| Binary                                           |  15.00 |  16.00 |  18.00 |   20.00 |
| Bernoulli [[8]](#note8) [[7]](#note7)            |   9.86 |  11.06 |  10.90 |   12.30 |
| gamma [[6]](#note6) [[4]](#note4)                |   6.51 |   5.68 |   4.48 |    6.63 |
| delta [[6]](#note6) [[4]](#note4)                |   6.23 |   5.08 |   4.35 |    6.38 |
| Observed frequency                               |   5.90 |   4.82 |   4.20 |    5.97 |
| *Local methods*                                  |        |        |        |         |
| Bernoulli [[14]](#note14) [[5]](#note5)          |   6.09 |   6.16 |   5.40 |    5.84 |
| Hyperbolic [[12]](#note12)                       |   5.75 |   5.16 |   4.65 |    5.89 |
| Skewed Bernoulli [[13]](#note13) [[11]](#note11) |   5.65 |   4.70 |   4.20 |    5.44 |
| Batched frequency [[11]](#note11)                |   5.58 |   4.64 |   4.02 |    5.41 |
| Interpolative [[10]](#note10)                    |   5.24 |   3.98 |   3.87 |    5.18 |



# References #

## Books ##

<a name="note1">1</a>: La Fontaine, J. de. "Fables". 1694. https://www.gutenberg.org/files/56327/56327-0.txt

<a name="note2">2</a>: Various. "Bible" (English translation). http://www.gutenberg.org/cache/epub/30/pg30.txt

<a name="note3">3</a>: I. H. Witten, A. Moffat, and T. C. Bell. "Managing Gigabytes". Morgan Kaufmann Publishing, San Francisco, 1999. https://people.eng.unimelb.edu.au/ammoffat/mg/

## Articles ##

<a name="note4">4</a>: J. L. Bentley and A. C.-C. Yao. “An almost optimal algorithm for unbounded searching”. Information Processing Letters 5.3 (1976), pp. 82–87. http://dx.doi.org/10.1016/0020-0190(76)90071-5

<a name="note5">5</a>: A. Bookstein, S. T. Klein, and T. Raita. “Model based concordance compression”. Proceedings of the 2nd Data Compression Conference (DCC). 1992. http://dx.doi.org/10.1109/DCC.1992.227473

<a name="note6">6</a>: P. Elias. “Universal Codeword Sets and Representations of the Integers”. IEEE Transactions on Information Theory 21.2 (1975), pp. 194–203. http://dx.doi.org/10.1109/TIT.1975.1055349

<a name="note7">7</a>: R. Gallager and D. van Voorhis. “Optimal source codes for geometrically distributed integer alphabets (Correspondence)”. IEEE Transactions on Information Theory 21.2 (1975), pp. 228–230. http://dx.doi.org/10.1109/TIT.1975.1055357

<a name="note8">8</a>: S. Golomb. “Run-length Encodings (Correspondence)”. IEEE Transactions on Information Theory 12.3 (1966), pp. 399–401. http://dx.doi.org/10.1109/TIT.1966.1053907

<a name="note9">9</a>: B. Klimt and Y. Yang. "The Enron Corpus: A New Dataset for Email Classification Research". Proceedings of the 15th European Conference on Machine Learning (ECML). Springer Berlin Heidelberg, 2004. http://dx.doi.org/10.1007/978-3-540-30115-8_22

<a name="note10">10</a>: A. Moffat and L. Stuiver. “Exploiting clustering in inverted file compression”. Proceedings of the 6th Data Compression Conference (DCC). 1996, pp. 82–91. http://dx.doi.org/10.1109/DCC.1996.488313

<a name="note11">11</a>: A. Moffat and J. Zobel. “Parameterised Compression for Sparse Bitmaps”. Proceedings of the 15th Annual International ACM SIGIR Conference on Research and Development in Information Retrieval. ACM, 1992, pp. 274–285. http://dx.doi.org/10.1145/133160.133210

<a name="note12">12</a>: E. J. Schuegraf. “Compression of large inverted files with hyperbolic term distribution”. Information Processing & Management 12.6 (1976), pp. 377–384. http://dx.doi.org/10.1016/0306-4573(76)90035-2

<a name="note13">13</a>: J. Teuhola. “A compression method for clustered bit-vectors”. Information Processing Letters 7.6 (1978), pp. 308–311. http://dx.doi.org/10.1016/0020-0190(78)90024-8

<a name="note14">14</a>: I. H. Witten, T. C. Bell, and C. G. Nevill. “Indexing and compressing full-text databases for CD-ROM”. Journal of Information Science 17.5 (1991), pp. 265–271. http://dx.doi.org/10.1177/016555159101700502

## Tools ##

<a name="note15">15</a>: The Apache Software Foundation. Apache Commons Primitives. 2003. https://commons.apache.org/dormant/commons-primitives/

<a name="note16">16</a>: The Apache Software Foundation. Apache JAMES Mime4j. 2004. https://james.apache.org/mime4j/

<a name="note17">17</a>: Jonathan Hedley. jsoup: Java HTML Parser. 2004. https://jsoup.org/

<a name="note18">18</a>: M. Porter and R. Boulton. Snowball. 2002. http://snowballstem.org/

