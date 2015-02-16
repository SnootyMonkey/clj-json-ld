(ns clj-json-ld.unit.value
  "
  Test value expansion as defined here: http://www.w3.org/TR/json-ld-api/#iri-expansion

  Test value compaction as defined here: http://www.w3.org/TR/json-ld-api/#iri-compaction
  "
  (:require [midje.sweet :refer :all]
            [clj-json-ld.value :refer (expand-value)]))

(def context {
  "ex" {"@id" "http://example.org/"}
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

    (fact "native date"
      (let [date-stamp (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd") "2011-12-27")]
        (expand-value context "foo", date-stamp) => {"@value" date-stamp}))

    (fact "native time"
      (let [time-stamp (.parse (java.text.SimpleDateFormat. "HH:mm:ss") "10:11:12Z")]
        (expand-value context "foo", time-stamp) => {"@value" time-stamp}))

    (fact "native dateTime"
      (let [date-time-stamp (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss") "2011-12-27T10:11:12Z")]
        (expand-value context "foo" date-time-stamp) => {"@value" date-time-stamp})))

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