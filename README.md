# TEXTBASE

It's a place where you can retrieve bits of texts of the whole humanity.

# Vision: Raisons d'être de Textbase
* commodification of art. No more vinyls, no more CDs, today we have Deezer and usb sticks. The art has becaome more and more (unfortunately) a resource. Etext.store is a collectiokn of small pieces of text that you can distribute, share etc. Not so much books (although you can read a book from beginning to end) but lists of shareable urls of literary wisdom.
* when it's ready: deezer of text (you can read and share payable texts).

Acestea sunt și aspectele ce fac unic Textbase. (egraphsen.com ?)

Deci nu este o editura de epuburi. Cărțile nu le citește nimeni.
Dar textul, partajabil, socializabil, trebuie să aibă o valoare. Aceasta est viziunea. Commodification of text.

History
===

Initially named scriptorium-repo. Just serve some TEIs.
There is also a project named TEI-publisher.
But: TEI is rather expensive. There are some TEI repos.
But docs: there are plenty. So our pipepline fodt -> tei -> web
might be on the long run, better.

- ODT format much easier to edit for starters, hence the originals in fodt (flat ODT)
- The idea is really to have a text database that you can access by URL, paragraph, word and letter.

GUI
---
- initially spring web, server-based
- for index/ and author/ now we have ionic/angular.
- site must remain crawlable and lynx-browseable
- right now, the teidiv.html template is server based. How to combile
the ionic under /app with the idea of having ordered URLS:
  - /author/work1/chapter1
- the /author is not that important. There are more relevant databases for famous people, like wikipedia etc.
- but /author/work/chapter is kind of the point of Textbase. That URL is the most proeminent way of querying Textbase.
- we need to have possible URLs like /author/work/chapter/div[0]/p[1] (an xpath subset that can identify fragments down to the letter)

Model economic
===
un url este mereu disponibil liber 
dar capacitatea de a naviga linear, de a citi o intreaga carte, nu e.
deci cautarea,care gaseste un url da.

dar inspectarea gratuita a unui url pune restrictii pentru vecinii sai imediati.

dupa un numar de next-uri gratuite pe zi, trebie sa cumperi abonament.

# Run Java server
```bash
$ gradle bootrun -Pprofile=dev,autoimport
```


# firefox

about:config

set security.fileuri.strict_origin_policy to false
in order to debug file:// html file with loading local resources.

# What is Textbase?

 A Database of wisdom, particularly addressable.
 Not a pile of ebooks: a very addressable vast space.
 Up to the letter.
 
 Ceea ce face particularitatea TB este adresabilitatea, dispozitia
 continutului interior pentru adresare si social sharing.
 
 Cele mai multe ebook stores vand ebookuri. TB ofera paragrafe, 
 in mare asta e ideea.
 
 De fapt TB nu vinde nimic, vinde abonamente.

# TODO

* address of a letter, of a paragraph, of a random range withing a book.
* search
=======
# misc
de vazut bookmate.com seamana foarte mult cu ce fac eu.
vand epuburi.

# INTERNALS

Saxon is used for XML processing, rather than the internal Xerces.

# Changlog
0.4 editii-util is now part of textbase.
last stable 537c3a3f5cd2229d790b0b832b5fea130e7a0d16


Apache/Angular fix
The problem of having an angular app (which manages with the Router its own 
URL) deployed on apache is making the two correspond.
One way to do it is the following, says the internet:
```apache
RewriteEngine On
# If an existing asset or directory is requested go to it as it is
RewriteCond %{DOCUMENT_ROOT}%{REQUEST_URI} -f [OR]
RewriteCond %{DOCUMENT_ROOT}%{REQUEST_URI} -d
RewriteRule ^ - [L]

# If the requested resource doesn't exist, use index.html
RewriteRule ^ /index.html
```
But as it didnt work for us, we used a SpringBoot solution which works well.  

# FEATURES

## Ionic/Angular UI

* deployed at /app/*
* replaces and modernizes /index.html /author.html
* the site must remaing crawlable by search engines and Links

## Relocation

The Relocation table stores HTTP relocation, mainly for the situation where
an URL of a well-known chapter has changed and should be preserved.

Insert a Relocation like this:
```bash
O=/old/path \
N=/new/path \
curl -X POST  -H 'Content-Type:application/json' \
https://textbase.scriptorium.ro/api/drest/relocations/ -d '{ "oldPath" : "$O", "newPath": "$N" }'
```


## Admin interface
* at /admin, protected by basic auth, interacts with /api/admin/*

# PRESENTATION

# a shareable digital library
  - a text database, semantically marked up
  - shareable fragments 


# but pretty too
  - https://textbase.scriptorium.ro/mitru/povesti_despre_pacala_si_tandala/tilharul_boierit
  https://textbase.scriptorium.ro/perrault/contes/peau_dane