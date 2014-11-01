(ns clj-json-ld.compaction
  (:require [midje.sweet :refer :all]
            [cheshire.core :refer (parse-string)]
            [clj-json-ld.lib.spec-test-suite :refer :all]
            [clj-json-ld.core :as json-ld]))

(def manifest "compact-manifest.jsonld")

(facts "Compact Evaluation Tests"

  (doseq [test-case (take 1 (tests-from-manifest manifest))]

    (println (:name test-case))

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
    (parse-string (json-ld/compact (:input test-case) (:context test-case))) =>
      (parse-string (:expect test-case))
  )
)