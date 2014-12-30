(ns clj-json-ld.spec.expansion
  (:require [midje.sweet :refer :all]
            [cheshire.core :refer (parse-string)]
            [clj-json-ld.util.spec-test-suite :refer :all]
            [clj-json-ld.core :as json-ld]))

(def manifest "expand-manifest.jsonld")

(facts "Expansion Evaluation Tests"

  (doseq [test-case (take 2 (tests-from-manifest manifest))]

    (print-test "Expansion" test-case)

    ;; Possible variations:
    ;; input - JSON, map, remote file
    ;; output - JSON, map

    ;; Combinatorial: 3 x 2 = 6 total cases

    ;; 1,"JSON","JSON"
    ;; 2,"JSON", "map"
    ;; 3,"map", "JSON"
    ;; 4,"map","map"
    ;; 5,"remote","JSON"
    ;; 6,"remote","map"

    ;; 1,"JSON","JSON"
    (parse-string (json-ld/expand (:input test-case))) =>
      (parse-string (:expect test-case))

    ;; 2,"JSON", "map"

    ;; 3,"map", "JSON"
    (parse-string (json-ld/expand (parse-string (:input test-case)))) =>
      (parse-string (:expect test-case))

    ;; 4,"map","map"

    ;; 5,"remote","JSON"

    ;; 6,"remote","map"

  )
)