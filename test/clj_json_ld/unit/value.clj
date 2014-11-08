(ns clj-json-ld.unit.value
  "
  Test value expansion as defined here: http://www.w3.org/TR/json-ld-api/#iri-expansion

  Test value compaction as defined here: http://www.w3.org/TR/json-ld-api/#iri-compaction
  "
  (:require [midje.sweet :refer :all]
            [clj-json-ld.value :refer (expand-value)]))

(def context {
  "ex" "http://example.org/"
  "ex:integer" {"@type" "xsd:integer"}
  "ex:double" {"@type" "xsd:double"}
  "ex:boolean" {"@type" "xsd:boolean"}
  "foaf:knows" {"@type" "@id"}
  "foaf:age" {"@type" "xsd:integer"}
  "dc:created" {"@type" "xsd:date"}
  "ja-lang-ex" {"@language" "ja"}
})

(def en-context (assoc context "@language" "en"))

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

  (facts "with no language mapping"
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
        (expand-value context "foo" date-time-stamp) => {"@value" date-time-stamp})))

    ;; Maybe wait on these until dealing with RDF? Maybe we don't even have this case
    ;; given how we'll be handling RDF?

    ;   "rdf boolean" =>    ["foo", RDF::Literal(true),             {"@value" => "true", "@type" => RDF::XSD.boolean.to_s}],
    ;   "rdf integer" =>    ["foo", RDF::Literal(1),                {"@value" => "1", "@type" => RDF::XSD.integer.to_s}],
    ;   "rdf decimal" =>    ["foo", RDF::Literal::Decimal.new(1.1), {"@value" => "1.1", "@type" => RDF::XSD.decimal.to_s}],
    ;   "rdf double" =>     ["foo", RDF::Literal::Double.new(1.1),  {"@value" => "1.1E0", "@type" => RDF::XSD.double.to_s}],
    ;   "rdf URI" =>        ["foo", RDF::URI("foo"),                {"@id" => "foo"}],
    ;   "rdf date " =>      ["foo", RDF::Literal(Date.parse("2011-12-27")), {"@value" => "2011-12-27", "@type" => RDF::XSD.date.to_s}],
    ;   "rdf nonNeg" =>     ["foo", RDF::Literal::NonNegativeInteger.new(1), {"@value" => "1", "@type" => RDF::XSD.nonNegativeInteger}],
    ;   "rdf float" =>      ["foo", RDF::Literal::Float.new(1.0), {"@value" => "1.0", "@type" => RDF::XSD.float}],

  (facts "with a default @language mapping of en"

    (fact "absolute IRI"
      (expand-value context "foaf:knows" "http://example.com/") => {"@id" "http://example.com/"})

    (fact "term"
      (expand-value context "foaf:knows" "ex") => {"@id" "ex"})

    (fact "prefix:suffix"
      (expand-value context "foaf:knows"  "ex:suffix") => {"@id" "http://example.org/suffix"})

    (fact "no IRI"
      (expand-value en-context "foo" "http://example.com/") => 
        {"@value" "http://example.com/" "@language" "en"})

      (fact "no term"
        (expand-value en-context "foo" "ex") => {"@value" "ex" "@language" "en"})

      (fact "no prefix"
        (expand-value en-context "foo" "ex:suffix") => {"@value" "ex:suffix" "@language" "en"})

      (fact "native boolean"
        (expand-value en-context "foo" true) => {"@value" true})

      (fact "native integer"
        (expand-value en-context "foo" 1) => {"@value" 1})

      (fact "native double"
        (expand-value en-context "foo" 1.1) => {"@value" 1.1}))

  (facts "with an @language mapping on the active property"
    ;; Test these once with no default language and once with 'en' as the default language mapping
    (doseq [active-context [context en-context]]

      (fact "no IRI"
        (expand-value active-context "ja-lang-ex" "http://example.com/") => 
          {"@value" "http://example.com/" "@language" "ja"})

      (fact "no term"
        (expand-value active-context "ja-lang-ex" "ex") => {"@value" "ex" "@language" "ja"})

      (fact "no prefix"
        (expand-value active-context "ja-lang-ex" "ex:suffix") => {"@value" "ex:suffix" "@language" "ja"})

      (fact "native boolean"
        (expand-value active-context "ja-lang-ex" true) => {"@value" true})

      (fact "native integer"
        (expand-value active-context "ja-lang-ex" 1) => {"@value" 1})

      (fact "native double"
        (expand-value active-context "ja-lang-ex" 1.1) => {"@value" 1.1})))

  (facts "with coerced @type members (that don't match the real type)"

    (fact "boolean-boolean"
      (expand-value en-context "ex:boolean" true) => {"@value" true "@type" "xsd:boolean"})

    (fact "boolean-integer"
      (expand-value en-context "ex:integer" true) => {"@value" true "@type" "xsd:integer"})

    (fact "boolean-double"
      (expand-value en-context "ex:double"  true) => {"@value" true "@type" "xsd:double"})

    (fact "double-boolean"
      (expand-value en-context "ex:boolean" 1.1) => {"@value" 1.1 "@type" "xsd:boolean"})

    (fact "double-double"
      (expand-value en-context "ex:double" 1.1) => {"@value" 1.1 "@type" "xsd:double"})

    (fact "double-integer"
      (expand-value en-context "foaf:age" 1.1) => {"@value" 1.1 "@type" "xsd:integer"})

    (fact "integer-boolean"
      (expand-value en-context "ex:boolean" 1) => {"@value" 1 "@type" "xsd:boolean"})

    (fact "integer-double"
       (expand-value en-context "ex:double" 1) => {"@value" 1 "@type" "xsd:double"})

    (fact "integer-integer"
      (expand-value en-context "foaf:age" 1) => {"@value" 1 "@type" "xsd:integer"})

    (fact "string-boolean"
      (expand-value en-context "ex:boolean" "foo") => {"@value" "foo" "@type" "xsd:boolean"})

    (fact "string-double" 
      (expand-value en-context "ex:double" "foo") => {"@value" "foo" "@type" "xsd:double"})

    (fact "string-integer"
      (expand-value en-context "foaf:age" "foo") => {"@value" "foo" "@type" "xsd:integer"})))