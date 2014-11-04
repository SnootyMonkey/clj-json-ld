(ns clj-json-ld.expand
  "
  Implement the Expansion operation as defined in the
  [Expansion Algorithm section](http://www.w3.org/TR/json-ld-api/#expansion-algorithm).
  "
  (:require [clj-json-ld.util.format :refer (ingest-input format-output)]))


(defn- expand [json]
  ;; Remove the context
  [(dissoc json "@context")])

(defn expand-it [input options]
  "[]")
  ;(format-output (expand (ingest-input input options)) options))