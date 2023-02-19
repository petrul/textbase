package ro.editii.scriptorium

import ro.editii.scriptorium.model.TeiDiv


def basedir = "~/es-gists".replace("~", System.getProperty("user.home"))
new File(basedir).mkdirs()

// def authors = auth_repo.findAll()
// def files = authors.collectMany { auth_repo.getTeiFiles(it.id); }

def alldivs = div_repo.findAll()

List<TeiDiv> leaves = alldivs.findAll { it.atBottom }

alldivs = null

files_created = leaves.collect {
    it.teiRepo = tei_repo
    final filename = it.completePath.replaceAll("/", "__")
    final f = new File(basedir, filename)
    
    if (! f.exists() ||  f.size() == 0) {
        println "$f ..."
        f.write(it.body)
    } else {
        println "ALREADY DONE IGNORING $f"
    }

    return filename
}

// url_no_slashes = url.replaceAll("/", "__")
return files_created
