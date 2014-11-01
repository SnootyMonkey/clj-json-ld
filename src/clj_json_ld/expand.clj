(ns clj-json-ld.expand
  (:require [clj-json-ld.util.format :refer (ingest-input format-output)]))

;; http://www.w3.org/TR/json-ld-api/#expansion-algorithm

(defn- expand [json]
  ;; Remove the context
  [(dissoc json "@context")])

(defn expand-it [input options]
  "[]")
  ;(format-output (expand (ingest-input input options)) options))