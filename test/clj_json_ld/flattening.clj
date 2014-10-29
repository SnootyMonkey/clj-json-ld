(ns clj-json-ld.flattening
  (:require [midje.sweet :refer :all]
            [clj-json-ld.core :refer :all]))

(def manifest "flatten-manifest.jsonld")

(fact "tautology"
  true => true)

(future-facts "Flatten Evaluation Tests")