(ns clj-json-ld.expansion
  (:require [midje.sweet :refer :all]
            [clj-json-ld.core :refer :all]))

(def manifest "expand-manifest.jsonld")

(fact "tautology"
  true => true)

(future-facts "Expansion Evaluation Tests")