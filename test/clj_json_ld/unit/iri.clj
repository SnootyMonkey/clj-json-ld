(ns clj-json-ld.unit.iri
  "
  Test IRI expansion as defined here: http://www.w3.org/TR/json-ld-api/#iri-expansion

  Test IRI compaction as defined here: http://www.w3.org/TR/json-ld-api/#iri-compaction
  "
  (:require [midje.sweet :refer :all]
            [clj-json-ld.json-ld :as json-ld]
            [clj-json-ld.iri :refer (expand-iri)]))

(def context {
  "@base" "http://base/"
  "@vocab" "http://vocab/"
  "ex" "http://example.org/"
  "" "http://empty/"
  "_" "http://underscore/"
})

(def document-relative {:document-relative true})
(def vocab {:vocab true})

(facts "about IRI Expansion"
 
  (facts "with no options"

    (fact "absolute IRI"
      (expand-iri context "http://example.org/") => "http://example.org/"
      (expand-iri context "ex://foo") => "ex://foo"
      (expand-iri context "foo:bar") => "foo:bar")

    (fact "term"
      (expand-iri context "ex") => "ex")

    (fact "prefix:suffix"
      (expand-iri context "ex:suffix") => "http://example.org/suffix")

    (fact "JSON-LD keyword"
      (doseq [json-ld-keyword json-ld/keywords]
        (expand-iri context json-ld-keyword) => json-ld-keyword))

    (fact "empty"
      (expand-iri context ":suffix") => "http://empty/suffix")

    (fact "unmapped"
      (expand-iri context "foo") => "foo")

    (fact "empty term"
      (expand-iri context "") => "")
    
    (fact "blank node"
      (expand-iri context "_:t0") => "_:t0")

    (fact "_"
      (expand-iri context "_") => "_"))

  (facts "with :document-relative true"

    (fact "absolute IRI"
      (expand-iri context "http://example.org/" document-relative) => "http://example.org/"
      (expand-iri context "ex://foo" document-relative) => "ex://foo"
      (expand-iri context "foo:bar" document-relative) => "foo:bar")

    (fact "term"
      (expand-iri context "ex" document-relative) => "http://base/ex")

    (fact "prefix:suffix"
      (expand-iri context "ex:suffix" document-relative) => "http://example.org/suffix")

    (fact "JSON-LD keyword"
      (doseq [json-ld-keyword json-ld/keywords]
        (expand-iri context json-ld-keyword document-relative) => json-ld-keyword))

    (fact "empty"
      (expand-iri context ":suffix" document-relative) => "http://empty/suffix")

    (fact "unmapped"
      (expand-iri context "foo" document-relative) => "http://base/foo")

    (fact "empty term"
      (expand-iri context "" document-relative) => "http://base/")

    (fact "blank node"
      (expand-iri context "_:t0" document-relative) => "_:t0")

    (fact "_"
      (expand-iri context "_" document-relative) => "http://base/_"))

  (facts "with :vocab true"

    (fact "absolute IRI"
      (expand-iri context "http://example.org/" vocab) => "http://example.org/"
      (expand-iri context "ex://foo" vocab) => "ex://foo"
      (expand-iri context "foo:bar" vocab) => "foo:bar")

    (fact "term"
      (expand-iri context "ex" vocab) => "http://example.org/")

    (fact "prefix:suffix"
      (expand-iri context "ex:suffix" vocab) => "http://example.org/suffix")

    (fact "JSON-LD keyword"
      (doseq [json-ld-keyword json-ld/keywords]
        (expand-iri context json-ld-keyword vocab) => json-ld-keyword))

    (fact "empty"
      (expand-iri context ":suffix" vocab) => "http://empty/suffix")

    (fact "unmapped"
      (expand-iri context "foo" vocab) => "http://vocab/foo")

    (fact "empty term"
      (expand-iri context "" vocab) => "http://empty/")

    (fact "blank node"
      (expand-iri context "_:t0" vocab) => "_:t0")

    (fact "_"
      (expand-iri context "_" vocab) => "http://underscore/")))