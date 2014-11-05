(ns clj-json-ld.value
  "
  Implement value expansion as defined in the
  [Value Expansion section](http://www.w3.org/TR/json-ld-api/#value-expansion).

  Implement value compaction as defined in the
  [Value Compaction section](http://www.w3.org/TR/json-ld-api/#value-compaction).
  "
  (:require [midje.sweet :refer :all]))

(future-facts "about value expansion")