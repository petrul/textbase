package ro.editii.scriptorium.gslt

import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil

//http://tatiyants.com/how-to-generate-xslt-using-groovy-streamingmarkupbuilder/
//class GsltProcessor {
//}

def ss = new StreamingMarkupBuilder().bind {
    mkp.declareNamespace(xsl: "http://www.w3.org/1999/XSL/Transform")

    xsl.stylesheet(version:"2.0") {
        xsl.template(match:"/") {
            html {
                head {
                    title("list of items")
                    style("asd")
                }
            }
            body {
                h1("items") {
                    ul {
                        xsl."apply-templates"(select:"items/item")
                    }
                }
            }
        }
        xsl.template(match:"item") {
            li {
                xsl."value-of"(select:"name")
            }
        }
    }
}

XmlUtil.serialize(ss, System.out)
