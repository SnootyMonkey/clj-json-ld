(ns clj-json-ld.unit.iri
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


(facts "about IRI Expansion"

; context "relative IRI" do
;   context "with no options" do
;     {
;       "absolute IRI" =>  ["http://example.org/", RDF::URI("http://example.org/")],
;       "term" =>          ["ex",                  RDF::URI("ex")],
;       "prefix:suffix" => ["ex:suffix",           RDF::URI("http://example.org/suffix")],
;       "keyword" =>       ["@type",               "@type"],
;       "empty" =>         [":suffix",             RDF::URI("http://empty/suffix")],
;       "unmapped" =>      ["foo",                 RDF::URI("foo")],
;       "empty term" =>    ["",                    RDF::URI("")],
;       "another abs IRI"=>["ex://foo",            RDF::URI("ex://foo")],
;       "absolute IRI looking like a curie" =>
;                          ["foo:bar",             RDF::URI("foo:bar")],
;       "bnode" =>         ["_:t0",                RDF::Node("t0")],
;       "_" =>             ["_",                   RDF::URI("_")],
;     }.each do |title, (input, result)|
;       it title do
;         expect(subject.expand_iri(input)).to produce(result, @debug)
;       end
;     end
;   end  
  (facts "with no options"

    (fact "absolute IRI"
      (expand-iri context "http://example.org/") => "http://example.org/"
      (expand-iri context "ex://foo") => "ex://foo"
      (expand-iri context "foo:bar") => "foo:bar")

    (fact "term"
      (expand-iri context "ex") => "ex")

    (future-fact "prefix:suffix"
      (expand-iri context "ex:suffix") => "http://example.org/suffix")

    (fact "JSON-LD keyword"
      (doseq [json-ld-keyword json-ld/keywords]
        (expand-iri context json-ld-keyword) => json-ld-keyword))

    (future-fact "empty"
      (expand-iri context ":suffix") => "http://empty/suffix")

    (fact "unmapped"
      (expand-iri context "foo") => "foo")

    (fact "empty term"
      (expand-iri context "") => "")
    
    (future-fact "blank node"
      (expand-iri context "_:t0") => "t0")

    (fact "_"
      (expand-iri context "_") => "_"))

; context "with base IRI" do
;   {
;     "absolute IRI" =>  ["http://example.org/", RDF::URI("http://example.org/")],
;     "term" =>          ["ex",                  RDF::URI("http://base/ex")],
;     "prefix:suffix" => ["ex:suffix",           RDF::URI("http://example.org/suffix")],
;     "keyword" =>       ["@type",               "@type"],
;     "empty" =>         [":suffix",             RDF::URI("http://empty/suffix")],
;     "unmapped" =>      ["foo",                 RDF::URI("http://base/foo")],
;     "empty term" =>    ["",                    RDF::URI("http://base/")],
;     "another abs IRI"=>["ex://foo",            RDF::URI("ex://foo")],
;     "absolute IRI looking like a curie" =>
;                        ["foo:bar",             RDF::URI("foo:bar")],
;     "bnode" =>         ["_:t0",                RDF::Node("t0")],
;     "_" =>             ["_",                   RDF::URI("http://base/_")],
;   }.each do |title, (input, result)|
;     it title do
;       expect(subject.expand_iri(input, :documentRelative => true)).to produce(result, @debug)
;     end
;   end
; end
  (future-facts "with :document-relative true")
    
; context "@vocab" do
;   {
;     "absolute IRI" =>  ["http://example.org/", RDF::URI("http://example.org/")],
;     "term" =>          ["ex",                  RDF::URI("http://example.org/")],
;     "prefix:suffix" => ["ex:suffix",           RDF::URI("http://example.org/suffix")],
;     "keyword" =>       ["@type",               "@type"],
;     "empty" =>         [":suffix",             RDF::URI("http://empty/suffix")],
;     "unmapped" =>      ["foo",                 RDF::URI("http://vocab/foo")],
;     "empty term" =>    ["",                    RDF::URI("http://empty/")],
;     "another abs IRI"=>["ex://foo",            RDF::URI("ex://foo")],
;     "absolute IRI looking like a curie" =>
;                        ["foo:bar",             RDF::URI("foo:bar")],
;     "bnode" =>         ["_:t0",                RDF::Node("t0")],
;     "_" =>             ["_",                   RDF::URI("http://underscore/")],
;   }.each do |title, (input, result)|
;     it title do
;       expect(subject.expand_iri(input, :vocab => true)).to produce(result, @debug)
;     end
;   end
  (future-facts "with :vocab true"))
