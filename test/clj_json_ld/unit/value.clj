(ns clj-json-ld.unit.value
  "
  Test value expansion as defined here: http://www.w3.org/TR/json-ld-api/#iri-expansion

  Test value compaction as defined here: http://www.w3.org/TR/json-ld-api/#iri-compaction
  "
  (:require [midje.sweet :refer :all]
            [clj-json-ld.value :refer (expand-value)]))

; ctx = context.parse({
;   "dc" => RDF::DC.to_uri.to_s,
;   "ex" => "http://example.org/",
;   "foaf" => RDF::FOAF.to_uri.to_s,
;   "xsd" => "http://www.w3.org/2001/XMLSchema#",
;   "foaf:age" => {"@type" => "xsd:integer"},
;   "foaf:knows" => {"@type" => "@id"},
;   "dc:created" => {"@type" => "xsd:date"},
;   "ex:integer" => {"@type" => "xsd:integer"},
;   "ex:double" => {"@type" => "xsd:double"},
;   "ex:boolean" => {"@type" => "xsd:boolean"},
; })

(def context {
  "ex" "http://example.org/"
  "foaf:knows" {"@type" "@id"}
  "foaf:age" {"@type" "xsd:integer"}
  "dc:created" {"@type" "xsd:date"}
})

(facts "about value expansion"

  ;; Email sent to JSON-LD list on Nov. 7th to determine if we should be
  ;; doing this. Don't see it in the spec. Maybe it's part of the RDF stuff?

  ;; Translation: if the value is literally a Ruby type, then expand
  ;; it to an IRI pointing to the W3C XML Schema IRI for the type
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
  
  (fact "integer"
    (expand-value context  "foaf:age" "54") => {"@value" "54" "@type" "xsd:integer"})

  (fact "date"
    (expand-value context "dc:created" "2011-12-27Z") => {"@value" "2011-12-27Z" "@type" "xsd:date"})

  (fact "native boolean"
    (expand-value context "foo" true) => {"@value" true})

  (fact "native integer"
    (expand-value context "foo" 1) => {"@value" 1})

  (fact "native double"
    (expand-value context "foo" 1.1e1) => {"@value" 1.1E1})

  ;; Our tests here differ from the Ruby lib's tests.
  ;; Email sent to JSON-LD list on Nov. 7th to determine if we should be
  ;; adding @type based on the native date/time/dateTime type, or only
  ;; based on the active context. Maybe it's part of the RDF stuff?

  (fact "native date"
    (let [date-stamp (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd") "2011-12-27")]
      (expand-value context "foo", date-stamp) => {"@value" date-stamp}))

  (fact "native time"
    (let [time-stamp (.parse (java.text.SimpleDateFormat. "HH:mm:ss") "10:11:12Z")]
      (expand-value context "foo", time-stamp) => {"@value" time-stamp}))

  (fact "native dateTime"
    (let [date-time-stamp (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss") "2011-12-27T10:11:12Z")]
      (expand-value context "foo" date-time-stamp) => {"@value" date-time-stamp}))

  ;; Maybe wait on these until dealing with RDF?

  ;   "rdf boolean" =>    ["foo", RDF::Literal(true),             {"@value" => "true", "@type" => RDF::XSD.boolean.to_s}],
  ;   "rdf integer" =>    ["foo", RDF::Literal(1),                {"@value" => "1", "@type" => RDF::XSD.integer.to_s}],
  ;   "rdf decimal" =>    ["foo", RDF::Literal::Decimal.new(1.1), {"@value" => "1.1", "@type" => RDF::XSD.decimal.to_s}],
  ;   "rdf double" =>     ["foo", RDF::Literal::Double.new(1.1),  {"@value" => "1.1E0", "@type" => RDF::XSD.double.to_s}],
  ;   "rdf URI" =>        ["foo", RDF::URI("foo"),                {"@id" => "foo"}],
  ;   "rdf date " =>      ["foo", RDF::Literal(Date.parse("2011-12-27")), {"@value" => "2011-12-27", "@type" => RDF::XSD.date.to_s}],
  ;   "rdf nonNeg" =>     ["foo", RDF::Literal::NonNegativeInteger.new(1), {"@value" => "1", "@type" => RDF::XSD.nonNegativeInteger}],
  ;   "rdf float" =>      ["foo", RDF::Literal::Float.new(1.0), {"@value" => "1.0", "@type" => RDF::XSD.float}],

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

)