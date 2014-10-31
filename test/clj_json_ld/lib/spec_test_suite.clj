(ns clj-json-ld.lib.spec-test-suite
  (:require [clojure.walk :refer (keywordize-keys)]
            [cheshire.core :refer (parse-string, parse-stream)]))

(def spec-location "../json-ld.org/")
(def tests-location (str spec-location "test-suite/tests/"))

(defn- load-manifest [manifest-file]
  (parse-stream (clojure.java.io/reader (str tests-location manifest-file))))

(defn tests-from-manifest
  "Load the :sequence value from the manifest file and replace
  the :input and :expect values with the JSON string from the file
  they point to."
  [manifest-file]
  (map #(assoc % :expect (slurp (str tests-location (:expect %))))
    (map #(assoc % :input (slurp (str tests-location (:input %))))
      (:sequence (keywordize-keys (load-manifest manifest-file))))))