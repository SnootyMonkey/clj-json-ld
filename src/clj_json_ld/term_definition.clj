(ns clj-json-ld.term-definition
  "
  6.2) Create Term Definition
  Term definitions are created by parsing the information in the given local context for the given term.
  "
  (:require [clojure.core.match :refer (match)]
            [defun :refer (defun defun-)]
            [clojurewerkz.urly.core :refer (absolute?)]
            [clj-json-ld.json-ld :as json-ld]
            [clj-json-ld.json-ld-error :refer (json-ld-error)]
            [clj-json-ld.iri :refer (expand-iri)]))

(defun- handle-type 
  ;; 10) If value contains the key @type:
  ([updated-context term value :guard #(contains? % "@type") local-context defined]

    ;; 10.1) Initialize type to the value associated with the @type key, which must be a string.
    ;; Otherwise, an invalid type mapping error has been detected and processing is aborted.
    (let [type (get value "@type")]
      
      (if-not (string? type)
        (json-ld-error "invalid type mapping error" (str "@type of term " term " in the local context is not a string.")))
      
      ;; 10.2) Set type to the result of using the IRI Expansion algorithm, passing active context, type for value,
      ;; true for vocab, false for document relative, local context, and defined....
      ;; is aborted.
      (let [expanded-type (expand-iri updated-context type {
                            :vocab true
                            :document-relative false
                            :local-context local-context
                            :defined defined})]
        ;; ...If the expanded type is neither @id, nor @vocab, nor an absolute IRI, an invalid type mapping
        ;; error has been detected and processing is aborted.
        (if-not (or (contains? #{"@id" "@vocab"} expanded-type) (absolute? expanded-type))
          (json-ld-error "invalid type mapping" (str "@type of term " term " in the local context is not valid.")))

        ;; 10.3) Set the type mapping for definition to type.
        (let [term-definition (or (get updated-context term) {})]
          (assoc updated-context term (assoc term-definition "@type" expanded-type))))))

  ; updated-context has no @type key, so do nothing
  ([updated-context _ _ _ _] 
    updated-context))

(defun- handle-reverse
  ;; 11) If value contains the key @reverse:
  ([updated-context term value :guard #(contains? % "@reverse")]
    updated-context)

  ; updated-context has no @reverse key, so do nothing
  ([updated-context _ _] updated-context))

(defn- handle-13-14-15 [updated-context term value]
  ;; 13) If value contains the key @id and its value does not equal term:
  ;; 14) Otherwise if the term contains a colon (:): 
  ;; 15) Otherwise, if active context has a vocabulary mapping, the IRI mapping of definition is set to the result of concatenating the value associated with the vocabulary mapping and term. If it does not have a vocabulary mapping, an invalid IRI mapping error been detected and processing is aborted.
  updated-context)

(defun- handle-container
  ;; 16) If value contains the key @container: 
  ([updated-context term value :guard #(contains? % "@container")]
    updated-context)

  ; updated-context has no @container key, so do nothing
  ([updated-context _ _] updated-context))

(defun- handle-language
  ;; 17) If value contains the key @language and does not contain the key @type: 
  ([updated-context term value :guard #(and (contains? % "@language") (not (contains? % "@type")))]
    updated-context)

  ; updated-context has no @language key, so do nothing
  ([updated-context _ _] updated-context))

(defn- new-term-definition
  "
  9) Create a new term definition, definition.
  "
  [active-context local-context term defined]

  (let [value (get local-context term)
        updated-context 
          (-> active-context
            ;; 10) If value contains the key @type: 
            (handle-type term value local-context defined)
            ;; 11) If value contains the key @reverse:
            (handle-reverse term value)    
            ;; 13) - 14) - 15)
            (handle-13-14-15 term value)
            ;; 16) If value contains the key @container: 
            (handle-container term value)
            ;; 17) If value contains the key @language and does not contain the key @type: 
            (handle-language term value))]
    ;; Return a tuple of the updated context and the defined map with the term marked as defined
    [updated-context (assoc defined term true)]))

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
  (let [defined-marker (get defined term)]
    (match [defined-marker] ; true, false or nil
      
      ; the term has already been created, so just return the result tuple
      [true] [active-context defined]
      
      ;; the term is in the process of being created, this is cyclical, oh noes!
      [false] (json-ld-error "cyclic IRI mapping"
            (str "local context has a term " term " that is used in its own definition"))
      
      ;; The term is not yet defined, so proceed to steps 2-18 of the algorithm
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

          (match [value]

            ;; 6) If value is null or value is a JSON object containing the key-value pair @id-null, set
            ;; the term definition in active context to null, set the value associated with defined's key
            ;; term to true, and return.
            ;; TODO revisit and simplify this logic
            [value :guard #(or 
                              (nil? %)
                              (and
                                (map? %)
                                (and 
                                  (contains? % "@id")
                                  (= (get % "@id") nil))))]
              ; return the result tuple with the term as nil
              [(assoc updated-context term nil) (assoc defined term true)]
  
            ;; 7) Otherwise, if value is a string, convert it to a JSON object consisting of a
            ;; single member whose key is @id and whose value is value.
            [value :guard string?] [(assoc updated-context term {"@id" value}) (assoc defined term true)]

            ;; 8) Otherwise, value must be a JSON object...
            [value :guard map?]
              ;; 9) Create a new term definition, definition.
              (new-term-definition active-context local-context term defined)

            ;; 8) ... if not, an invalid term definition
            ;; error has been detected and processing is aborted.
            [value] (json-ld-error "invalid term definition" (str "The term " term " in the local context is not valid."))

          )
        )
      ;; 2) Set the value associated with defined's term key to false.
      ;; This indicates that the term definition is now being created but is not yet complete.
      ;; 14.1 recurses back into this algorithm
      
      ;;18) Set the term definition of term in active context to definition and set the value
      ;; associated with defined's key term to true.      
      )
    )
  )
)