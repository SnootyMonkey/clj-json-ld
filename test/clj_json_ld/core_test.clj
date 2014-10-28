(ns clj-json-ld.core-test
  (:require [midje.sweet :refer :all]
            [clj-json-ld.core :refer :all]))

(fact "tautology"
  true => true)