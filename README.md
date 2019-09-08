# SearchAndCrypt

*What is the purpose of our library?*

The main goal of this library is to enable searching functionalities inside a
set of encrypted e-mails, stored on a distant server. To this end, we rely on
the management of an inverted index which stores, for each word which is in the
e-mails, the set of e-mails which contain this word. This index is then stored,
encrypted, on the server. Whenever an e-mail is added (the user receives a new
e-mail or writes a new e-mail) or is removed (the user deletes an old e-mail),
the index has to be updated. Whenever the user wants to perform a search in
their e-mails, they type some words, and the library will show them the set of
documents that contain all the words they have typed.


*A simple example.*

Let us start with a simple example, taken from "Managing Gigabytes"<sup>[1](#myfootnote1)</sup>. For
now, we will omit the technical details which are part of the
representation of an e-mail, and will just consider a set of documents to
index. Table 1 shows a set of 6 documents that we want
to index. Table 2 shows the inverted index of this set
of documents (sorted alphabetically, in ascending order). The word "cold" is
part of 2 documents: document number 1, and document number 4 ; the word
"days" is part of 2 documents: document number 3, and document number 6 ;
etc.

Table 1: Example text; each line is one document.

| Document |                    Text                  |
|----------|:----------------------------------------:|
|     1    | Pease porridge hot, pease porridge cold, |
|     2    | Pease porridge in the pot,               |
|     3    | Nine days old.                           |
|     4    | Some like it hot, some like it cold,     |
|     5    | Some like it in the pot,                 |
|     6    | Nine days old.                           |


Table 2: Inverted file for text of Table 1.

| Number |   Term   | Documents |
|--------|:--------:|-----------|
|    1   | cold     | {1,4}     |
|    2   | days     | {3,6}     |
|    3   | hot      | {1,4}     |
|    4   | in       | {2,5}     |
|    5   | it       | {4,5}     |
|    6   | like     | {4,5}     |
|    7   | nine     | {3,6}     |
|    8   | old      | {3,6}     |
|    9   | pease    | {1,2}     |
|   10   | porridge | {1,2}     |
|   11   | pot      | {2,5}     |
|   12   | some     | {4,5}     |
|   13   | the      | {2,5}     |

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

<a name="myfootnote1">1</a>: I. H. Witten, A. Moffat, and T. C. Bell. "Managing Gigabytes". Morgan Kaufmann Publishing, San Francisco, 1999. https://people.eng.unimelb.edu.au/ammoffat/mg/

