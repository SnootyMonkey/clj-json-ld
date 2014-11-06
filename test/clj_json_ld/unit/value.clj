(ns clj-json-ld.unit.value
  "
  Test value expansion as defined here: http://www.w3.org/TR/json-ld-api/#iri-expansion

  Test value compaction as defined here: http://www.w3.org/TR/json-ld-api/#iri-compaction
  "
  (:require [midje.sweet :refer :all]
            [clj-json-ld.value :refer (expand-value)]))

; subject {
;   ctx = context.parse({
;     "dc" => RDF::DC.to_uri.to_s,
;     "ex" => "http://example.org/",
;     "foaf" => RDF::FOAF.to_uri.to_s,
;     "xsd" => "http://www.w3.org/2001/XMLSchema#",
;     "foaf:age" => {"@type" => "xsd:integer"},
;     "foaf:knows" => {"@type" => "@id"},
;     "dc:created" => {"@type" => "xsd:date"},
;     "ex:integer" => {"@type" => "xsd:integer"},
;     "ex:double" => {"@type" => "xsd:double"},
;     "ex:boolean" => {"@type" => "xsd:boolean"},
;   })
;   @debug.clear
;   ctx
; }
(def context {
  "ex" "http://example.org/"
  "foaf:knows" {"@type" "@id"}
})

(facts "about value expansion"

; What's this??
 ; %w(boolean integer string dateTime date time).each do |dt|
 ;    it "expands datatype xsd:#{dt}" do
 ;      expect(subject.expand_value("foo", RDF::XSD[dt])).to produce({"@id" => "http://www.w3.org/2001/XMLSchema##{dt}"}, @debug)
 ;    end
 ;  end

  (fact "absolute IRI"
    (expand-value context "foaf:knows" "http://example.com/") => {"@id" "http://example.com/"})

  (fact "term"
    (expand-value context "foaf:knows" "ex") => {"@id" "ex"})

  (fact "prefix:suffix"
    (expand-value context "foaf:knows"  "ex:suffix") => {"@id" "http://example.org/suffix"})

  (fact "no IRI"
    (expand-value context  "foo" "http://example.com/") => {"@value" "http://example.com/"})

  (fact "no term"
    (expand-value context "foo" "ex") => {"@value" "ex"})

  (fact "no prefix"
    (expand-value context "foo" "ex:suffix") => {"@value" "ex:suffix"})
  
;   "integer" =>        ["foaf:age",    "54",                   {"@value" => "54", "@type" => RDF::XSD.integer.to_s}],
;   "date " =>          ["dc:created",  "2011-12-27Z",          {"@value" => "2011-12-27Z", "@type" => RDF::XSD.date.to_s}],
;   "native boolean" => ["foo", true,                           {"@value" => true}],
;   "native integer" => ["foo", 1,                              {"@value" => 1}],
;   "native double" =>  ["foo", 1.1e1,                          {"@value" => 1.1E1}],
;   "native date" =>    ["foo", Date.parse("2011-12-27"),       {"@value" => "2011-12-27", "@type" => RDF::XSD.date.to_s}],
;   "native time" =>    ["foo", Time.parse("10:11:12Z"),        {"@value" => "10:11:12Z", "@type" => RDF::XSD.time.to_s}],
;   "native dateTime" =>["foo", DateTime.parse("2011-12-27T10:11:12Z"), {"@value" => "2011-12-27T10:11:12Z", "@type" => RDF::XSD.dateTime.to_s}],
;   "rdf boolean" =>    ["foo", RDF::Literal(true),             {"@value" => "true", "@type" => RDF::XSD.boolean.to_s}],
;   "rdf integer" =>    ["foo", RDF::Literal(1),                {"@value" => "1", "@type" => RDF::XSD.integer.to_s}],
;   "rdf decimal" =>    ["foo", RDF::Literal::Decimal.new(1.1), {"@value" => "1.1", "@type" => RDF::XSD.decimal.to_s}],
;   "rdf double" =>     ["foo", RDF::Literal::Double.new(1.1),  {"@value" => "1.1E0", "@type" => RDF::XSD.double.to_s}],
;   "rdf URI" =>        ["foo", RDF::URI("foo"),                {"@id" => "foo"}],
;   "rdf date " =>      ["foo", RDF::Literal(Date.parse("2011-12-27")), {"@value" => "2011-12-27", "@type" => RDF::XSD.date.to_s}],
;   "rdf nonNeg" =>     ["foo", RDF::Literal::NonNegativeInteger.new(1), {"@value" => "1", "@type" => RDF::XSD.nonNegativeInteger}],
;   "rdf float" =>      ["foo", RDF::Literal::Float.new(1.0), {"@value" => "1.0", "@type" => RDF::XSD.float}],
; }.each do |title, (key, compacted, expanded)|
;   it title do
;     expect(subject.expand_value(key, compacted)).to produce(expanded, @debug)
;   end
; end

)

; context "@language" do
;   before(:each) {subject.default_language = "en"}
;   {
;     "no IRI" =>         ["foo",         "http://example.com/",  {"@value" => "http://example.com/", "@language" => "en"}],
;     "no term" =>        ["foo",         "ex",                   {"@value" => "ex", "@language" => "en"}],
;     "no prefix" =>      ["foo",         "ex:suffix",            {"@value" => "ex:suffix", "@language" => "en"}],
;     "native boolean" => ["foo",         true,                   {"@value" => true}],
;     "native integer" => ["foo",         1,                      {"@value" => 1}],
;     "native double" =>  ["foo",         1.1,                    {"@value" => 1.1}],
;   }.each do |title, (key, compacted, expanded)|
;     it title do
;       expect(subject.expand_value(key, compacted)).to produce(expanded, @debug)
;     end
;   end
; end

; context "coercion" do
;   before(:each) {subject.default_language = "en"}
;   {
;     "boolean-boolean" => ["ex:boolean", true,   {"@value" => true, "@type" => RDF::XSD.boolean.to_s}],
;     "boolean-integer" => ["ex:integer", true,   {"@value" => true, "@type" => RDF::XSD.integer.to_s}],
;     "boolean-double"  => ["ex:double",  true,   {"@value" => true, "@type" => RDF::XSD.double.to_s}],
;     "double-boolean"  => ["ex:boolean", 1.1,    {"@value" => 1.1, "@type" => RDF::XSD.boolean.to_s}],
;     "double-double"   => ["ex:double",  1.1,    {"@value" => 1.1, "@type" => RDF::XSD.double.to_s}],
;     "double-integer"  => ["foaf:age",   1.1,    {"@value" => 1.1, "@type" => RDF::XSD.integer.to_s}],
;     "integer-boolean" => ["ex:boolean", 1,      {"@value" => 1, "@type" => RDF::XSD.boolean.to_s}],
;     "integer-double"  => ["ex:double",  1,      {"@value" => 1, "@type" => RDF::XSD.double.to_s}],
;     "integer-integer" => ["foaf:age",   1,      {"@value" => 1, "@type" => RDF::XSD.integer.to_s}],
;     "string-boolean"  => ["ex:boolean", "foo",  {"@value" => "foo", "@type" => RDF::XSD.boolean.to_s}],
;     "string-double"   => ["ex:double",  "foo",  {"@value" => "foo", "@type" => RDF::XSD.double.to_s}],
;     "string-integer"  => ["foaf:age",   "foo",  {"@value" => "foo", "@type" => RDF::XSD.integer.to_s}],
;   }.each do |title, (key, compacted, expanded)|
;     it title do
;       expect(subject.expand_value(key, compacted)).to produce(expanded, @debug)
;     end
;   end