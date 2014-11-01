(ns clj-json-ld.json-ld)

(def keywords #{
  "@context" ; Used to define the short-hand names that are used throughout a JSON-LD document. These short-hand names are called terms and help developers to express specific identifiers in a compact manner. The @context keyword is described in detail in section 5.1 The Context.
  "@id" ; Used to uniquely identify things that are being described in the document with IRIs or blank node identifiers. This keyword is described in section 5.3 Node Identifiers.
  "@value" ; Used to specify the data that is associated with a particular property in the graph. This keyword is described in section 6.9 String Internationalization and section 6.4 Typed Values.
  "@language" ; Used to specify the language for a particular string value or the default language of a JSON-LD document. This keyword is described in section 6.9 String Internationalization.
  "@type" ;Used to set the data type of a node or typed value. This keyword is described in section 6.4 Typed Values.
  "@container" ; Used to set the default container type for a term. This keyword is described in section 6.11 Sets and Lists.
  "@list" ; Used to express an ordered set of data. This keyword is described in section 6.11 Sets and Lists.
  "@set" ; Used to express an unordered set of data and to ensure that values are always represented as arrays. This keyword is described in section 6.11 Sets and Lists.
  "@reverse" ; Used to express reverse properties. This keyword is described in section 6.12 Reverse Properties.
  "@index" ; Used to specify that a container is used to index information and that processing should continue deeper into a JSON data structure. This keyword is described in section 6.16 Data Indexing.
  "@base" ;Used to set the base IRI against which relative IRIs are resolved. This keyword is described in section 6.1 Base IRI.
  "@vocab" ; Used to expand properties and values in @type with a common prefix IRI. This keyword is described in section 6.2 Default Vocabulary.
  "@graph" ; Used to express a graph. This keyword is described in section 6.13 Named Graphs.
})

(defn json-ld-keyword?
  "Returns `true` if the value is a JSON-LD keyword, otherwise `false`."
  [value]
  (if (keywords value) true false))