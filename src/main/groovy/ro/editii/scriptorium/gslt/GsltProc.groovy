package ro.editii.scriptorium.gslt

class GsltProc {

    def hi() {
        println('hi there')
    }

    static void main(String[] args) {
        final script = args[0];
        final gse = new GroovyScriptEngine( '.' ).with {
            it.class.mixin(GsltProc.class)
            loadScriptByName( script )
        }
        gse.run()

    }
}
