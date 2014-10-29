(ns clj-json-ld.compaction
  (:require [midje.sweet :refer :all]
            [clj-json-ld.core :refer (compact)]))

(def manifest "compaction-manifest.jsonld")

(fact "tautology"
  true => true)

(future-facts "Compact Evaluation Tests")