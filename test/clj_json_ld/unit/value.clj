(ns clj-json-ld.unit.value
  "
  Test value expansion as defined here: http://www.w3.org/TR/json-ld-api/#iri-expansion

  Test value compaction as defined here: http://www.w3.org/TR/json-ld-api/#iri-compaction
  "
  (:require [midje.sweet :refer :all]
            [clj-json-ld.json-ld :as json-ld]
            [clj-json-ld.iri :refer (expand-iri)]))
