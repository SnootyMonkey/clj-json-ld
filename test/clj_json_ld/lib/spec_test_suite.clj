(ns clj-json-ld.lib.spec-test-suite
  (:require [clojure.walk :refer (keywordize-keys)]
            [cheshire.core :refer (parse-string, parse-stream)]))

(def spec-location "../json-ld.org/")
(def tests-location (str spec-location "test-suite/tests/"))

(defn- load-manifest [manifest-file]
  (parse-stream (clojure.java.io/reader (str tests-location manifest-file))))

(defn tests-from-manifest
  "Load the :sequence vector from the manifest file and replace
  the :input, :expect and :context values in each test case with the
  JSON string contents of the file they point to."
  [manifest-file]
  (->> (:sequence (keywordize-keys (load-manifest manifest-file)))
    (map #(assoc % :input (slurp (str tests-location (:input %)))))
    (map #(assoc % :expect (slurp (str tests-location (:expect %)))))
    (map #(assoc % :context (if (:context %) (slurp (str tests-location (:context %))) nil)))))