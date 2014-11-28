(ns clj-json-ld.term-definition
  "Term definitions are created by parsing the information in the given local context for the given term."
  (:require [clojure.core.match :refer (match)]
            [clj-json-ld.json-ld-error :refer (json-ld-error)]))


(defn create-term-definition
  "
  Algorithm called from the Context Processing algorithm to create a term definition in the
  active context for a term being processed in a local context.
  "
  [active-context local-context term defined]
  
  ;; 1) If defined contains the key term and the associated value is true
  ;; (indicating that the term definition has already been created), return.
  ;; Otherwise, if the value is false, a cyclic IRI mapping error has been detected
  ;; and processing is aborted.
  (let [defined-value (get defined term)]
    (match [defined-value]
      
      [true] [active-context defined] ; the term has already been created, so just return
      
      [defined-value :guard nil?] [active-context defined] ; temporary no-op
      
      ;; the term is in the process of being created, this is cyclical, oh noes!
      [_] (json-ld-error "cyclic IRI mapping"
            (str "local context has a term " term " that is used in its own definition")))))