(ns clj-json-ld.term-definition
  "
  6.2) Create Term Definition
  Term definitions are created by parsing the information in the given local context for the given term.
  "
  (:require [clojure.core.match :refer (match)]
            [clj-json-ld.json-ld :as json-ld]
            [clj-json-ld.json-ld-error :refer (json-ld-error)]))

(defn create-term-definition
  "
  6.2) Create Term Definition

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
      
      [true] [active-context defined] ; the term has already been created, so just return the result tuple
      
      [false] (json-ld-error "cyclic IRI mapping"
            (str "local context has a term " term " that is used in its own definition"))
      
      ;; The term is not defined, so proceed to steps (2-18)
      [_] (do

        ;; 3) Since keywords cannot be overridden, the term must not be a keyword. Otherwise, a
        ;; keyword redefinition error has been detected and processing is aborted.
        (if (json-ld/keywords term) 
          (json-ld-error "keyword redefinition"
            (str term " is a keyword and can't be used in a local context")))

        ;; 4) Remove any existing term definition for term in active context.
        ;; 5) Initialize value to a copy of the value associated with the key term in local context.
        (let [updated-context (dissoc active-context term)
              value (get local-context term)]

          ;; 6) If value is null or value is a JSON object containing the key-value pair @id-null, set
          ;; the term definition in active context to null, set the value associated with defined's key
          ;; term to true, and return.
          (if (or (nil? value) (and (map? value) (= (get value "@id") nil)))
            ; return the result tuple with the term as nil
            [(assoc updated-context term nil) (assoc defined term true)]
  
            ;; 7) Otherwise, if value is a string, convert it to a JSON object consisting of a
            ;; single member whose key is @id and whose value is value.

            ;; 8) Otherwise, value must be a JSON object, if not, an invalid term definition
            ;; error has been detected and processing is aborted.
          )
        )
      ;; 2) Set the value associated with defined's term key to false.
      ;; This indicates that the term definition is now being created but is not yet complete.
      ;; 14.1 recurses back into this algorithm
      
      ;;18) Set the term definition of term in active context to definition and set the value
      ;; associated with defined's key term to true.      
      ;; the term is in the process of being created, this is cyclical, oh noes!
      )
    )
  )
)