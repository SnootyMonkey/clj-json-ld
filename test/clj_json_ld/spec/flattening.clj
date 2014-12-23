(ns clj-json-ld.spec.flattening
  (:refer-clojure :exclude [flatten])
  (:require [midje.sweet :refer :all]
            [cheshire.core :refer (parse-string)]
            [clj-json-ld.util.spec-test-suite :refer :all]
            [clj-json-ld.core :as json-ld]))

(def manifest "flatten-manifest.jsonld")

(facts "Flatten Evaluation Tests"

  (doseq [test-case (take 1 (tests-from-manifest manifest))]

    (print-test "Flatten" test-case)

    ;; Possible variation:
    ;; input - JSON, map, remote file
    ;; context - JSON, map, remote file
    ;; output - JSON, map

    ;; Combinatorial: 3 x 3 x 2 = 18 total cases

    ;; Optimal pairwise test set of 9:
    ;; 1,"JSON","JSON","JSON"
    ;; 2,"map","JSON","map"
    ;; 3,"JSON","map","map"
    ;; 4,"map","map","JSON"
    ;; 5,"remote","JSON","JSON"
    ;; 6,"JSON","remote","JSON"
    ;; 7,"map","remote","map"
    ;; 8,"remote","map","map"
    ;; 9,"remote","remote","JSON"

    ;; 1,"JSON","JSON","JSON"
    (parse-string (json-ld/flatten (:input test-case) (:context test-case))) =>
      (parse-string (:expect test-case))

    ;; 2,"map","JSON","map"

    ;; 3,"JSON","map","map"

    ;; 4,"map","map","JSON"
    (parse-string (json-ld/flatten (parse-string (:input test-case))
      (parse-string (:context test-case)))) => (parse-string (:expect test-case))

    ;; 5,"remote","JSON","JSON"

    ;; 6,"JSON","remote","JSON"

    ;; 7,"map","remote","map"

    ;; 8,"remote","map","map"

    ;; 9,"remote","remote","JSON"

  )
)