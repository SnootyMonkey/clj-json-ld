(ns clj-json-ld.util.format
  (:require [cheshire.core :refer (parse-string generate-string)]))

(defn ingest-input
  ""
  [input options] 
  ;; Parse the JSON if need be
  (if (map? input) input (parse-string input)))
 
(defn format-output
  ""
  [json options]
  (generate-string json))