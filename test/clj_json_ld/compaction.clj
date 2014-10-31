(ns clj-json-ld.compaction
  (:require [midje.sweet :refer :all]
            [clj-json-ld.core :refer (compact)]))

(def manifest "compaction-manifest.jsonld")

(future-facts "Compact Evaluation Tests"

  (doseq [test-case (tests-from-manifest manifest)]

    (println (:name test))

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
    )
)