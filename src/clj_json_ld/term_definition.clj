(ns clj-json-ld.term-definition
  "
  Implement the create term definition algorithm as defined in
  the [Create Term Definition section](http://www.w3.org/TR/json-ld-api/#create-term-definition).

  Term definitions are created by parsing the information in the given local context for the given term.
  "
  (:require [clojure.string :as s]
            [clojure.core.match :refer (match)]
            [defun :refer (defun defun-)]
            [clj-json-ld.json-ld :as json-ld]
            [clj-json-ld.json-ld-error :refer (json-ld-error)]
            [clj-json-ld.iri :refer (expand-iri blank-node-identifier? absolute-iri?)]))

(defun- handle-type 
  ;; 10) If value contains the key @type:
  ([updated-context term value :guard #(contains? % "@type") local-context defined]

    ;; 10.1) Initialize type to the value associated with the @type key...
    (let [type (get value "@type")]
      
      ;; ... which must be a string. Otherwise, an invalid type mapping error has been detected and processing is
      ;; aborted.
      (if-not (string? type)
        (json-ld-error "invalid type mapping error" (str "@type of term " term " in the local context is not a string.")))
      
      ;; 10.2) Set type to the result of using the IRI Expansion algorithm, passing active context, type for value,
      ;; true for vocab, false for document relative, local context, and defined....
      (let [expanded-type (expand-iri updated-context type {
                            :vocab true
                            :document-relative false
                            :local-context local-context
                            :defined defined})]
        ;; ...If the expanded type is neither @id, nor @vocab, nor an absolute IRI, an invalid type mapping
        ;; error has been detected and processing is aborted.
        (if-not (or (contains? #{"@id" "@vocab"} expanded-type) (absolute-iri? expanded-type))
          (json-ld-error "invalid type mapping" (str "@type of term " term " in the local context is not valid.")))

        ;; 10.3) Set the type mapping for definition to type.
        (let [term-definition (or (get updated-context term) {})]
          (assoc updated-context term (assoc term-definition "@type" expanded-type))))))

  ; updated-context has no @type key, so do nothing
  ([updated-context _ _ _ _] 
    updated-context))

(defun- add-optional-container
  "Add the value of @container in the term to the term definition map, with the key @container"
  ([value :guard #(contains? % "@container") term-definition]
    (assoc term-definition "@container" (get value "@container")))
  ([_ term-definition] term-definition)) ; value doesn't contain @container, so do nothing

(defun- handle-reverse
  ;; 11) If value contains the key @reverse:
  ([updated-context term value :guard #(contains? % "@reverse") local-context defined]

    ;; 11.1) If value contains an @id member, an invalid reverse property error has been detected and processing
    ;; is aborted.
    (if (contains? value "@id") (json-ld-error "invalid reverse property" (str term " contains both @reverse and @id")))
    
    (let [reverse-value (get value "@reverse")]

      ;; 11.2) If the value associated with the @reverse key is not a string, an invalid IRI mapping error has been
      ;; detected and processing is aborted.
      (if-not (string? reverse-value)
        (json-ld-error "invalid IRI mapping" (str "The value of @reverse for term " term " is not a string.")))

      ;; 11.3) Otherwise, set the IRI mapping of definition to the result of using the IRI Expansion algorithm,
      ;; passing active context, the value associated with the @reverse key for value, true for vocab, false for
      ;; document relative, local context, and defined. ...
      (let [expanded-reverse (expand-iri updated-context reverse-value {
                            :vocab true
                            :document-relative false
                            :local-context local-context
                            :defined defined})]
        ;; ... If the result is neither an absolute IRI nor a blank node identifier, i.e., it contains no colon (:),
        ;; an invalid IRI mapping error has been detected and processing is aborted.
        (if-not (or (absolute-iri? expanded-reverse) (blank-node-identifier? expanded-reverse))
          (json-ld-error "invalid IRI mapping error"
            (str "The value of @reverse for term " term " is not a absolute IRI or a blank node identifier.")))
    
        ;; 11.4) If value contains an @container member, ... if its value is neither @set, nor @index, nor null,
        ;; an invalid reverse property error has been detected (reverse properties only support set- and
        ;; index-containers) and processing is aborted.
        (if (contains? value "@container") 
          (let [container-value (get value "@container")]
            (if-not (or (= container-value "@set") (= container-value "@index") (= container-value nil))
              (json-ld-error "invalid reverse property" (str "The value of @container for term " term " was not @set, @index or null.")))))

        ;; 11.4) If value contains an @container member, set the container mapping of definition to its value
        ;; 11.5) Set the reverse property flag of definition to true.
        ;; 11.6) Set the term definition of term in active context to definition and the value associated with
        ;; defined's key term to true and return.
        (let [term-definition (or (get updated-context term) {})]
          (assoc updated-context term
            (add-optional-container value
              (-> term-definition
                (assoc "@reverse" expanded-reverse)
                (assoc :reverse true))))))))

  ; updated-context has no @reverse key, so do nothing
  ([updated-context _ _ _ _] updated-context))

(defn- match-13?
  "
  Given a tuple of term and value, return true if this condition holds:
  13) If value contains the key @id and its value does not equal term:
  "
  [term-value]
  (let [term (first term-value)
        value (last term-value)
        id? (contains? value "@id")
        id-value (get value "@id")]
    (and id? (not (= id-value term)))))

(defun- handle-iri-mapping
  "Potential match for each mutually exclusive case of IRI mapping: 13, 14 and 15."
  
  ;; 13) If value contains the key @id and its value does not equal term:
  ([updated-context term-value :guard match-13? local-context defined]
    
    ;; 13.1) If the value associated with the @id key is not a string, an invalid IRI mapping error has been
    ;; detected and processing is aborted.
    (let [term (first term-value)
          value (last term-value)
          id-value (get value "@id")]
      (if-not (string? id-value)
        (json-ld-error "invalid IRI mapping" (str "The value of @id for term " term " was not a string.")))
    
      ;; 13.2) Otherwise, set the IRI mapping of definition to the result of using the IRI Expansion algorithm,
      ;; passing active context, the value associated with the @id key for value, true for vocab, false for document
      ;; relative, local context, and defined. ...
      (let [iri-mapping (expand-iri updated-context id-value {
                            :vocab true
                            :document-relative false
                            :local-context local-context
                            :defined defined})]

        ;; ... If the resulting (expanded) IRI mapping is neither a keyword, nor an absolute IRI,
        ;; nor a blank node identifier, an invalid IRI mapping error has been detected
        ;; and processing is aborted; ...
        (if-not (or 
                  (contains? json-ld/keywords iri-mapping)
                  (absolute-iri? iri-mapping)
                  (blank-node-identifier? iri-mapping))
          (json-ld-error "invalid IRI mapping"
            (str "The value of @id for term " term " was not a JSON-LD keyword, an absolute IRI, or a blank node identifier.")))
        
        ;; ... if it equals @context, an invalid keyword alias error has been detected and processing is aborted.
        (if (= iri-mapping "@context")
          (json-ld-error "invalid keyword alias" (str "The value of @id for term " term " cannot be @context.")))

        ;; set the IRI mapping of definition to the result of using the IRI Expansion algorithm
        (let [term-definition (or (get updated-context term) {})]
          (assoc updated-context term (assoc term-definition "@id" iri-mapping))))))

  ;; 14) Otherwise if the term contains a colon (:): 
  
  ;; 15) Otherwise, if active context has a vocabulary mapping, the IRI mapping of definition is set to the result of concatenating the value associated with the vocabulary mapping and term. If it does not have a vocabulary mapping, an invalid IRI mapping error been detected and processing is aborted.
  ([updated-context _ _ _] updated-context))

(defun- handle-container
  ;; 16) If value contains the key @container: 
  ([updated-context term value :guard #(contains? % "@container")]

    ;; 16.1) Initialize container to the value associated with the @container key, ...
    (let [container-value (get value "@container")]
      ;; ... which must be either @list, @set, @index, or @language. Otherwise, an
      ;; invalid container mapping error has been detected and processing is aborted.
      (if-not (contains? #{"@list" "@set" "@index" "@language"} container-value)
        (json-ld-error "invalid container mapping" (str "The value of @container for term " term " was not @list, @set, @index or @language.")))
  
      ;; 16.2) Set the container mapping of definition to container.
      (let [term-definition (or (get updated-context term) {})]
        (assoc updated-context term (assoc term-definition "@container" container-value)))))

  ; updated-context has no @container key, so do nothing
  ([updated-context _ _] updated-context))

(defun- handle-language
  ;; 17) If value contains the key @language and does not contain the key @type: 
  ([updated-context term value :guard #(and (contains? % "@language") (not (contains? % "@type")))]
    
    ;; 17.1) Initialize language to the value associated with the @language key, which must be either null or
    ;; a string. Otherwise, an invalid language mapping error has been detected and processing is aborted.
    (let [language (get value "@language")]
      (if-not (or (string? language) (nil? language))
        (json-ld-error "invalid language mapping" (str "The value of @language for term " term " was not a string or null.")))
  
        ;; 17.2) If language is a string set it to lowercased language.
        ;; Set the language mapping of definition to language.
        (let [term-definition (or (get updated-context term) {})]
          (assoc updated-context term
            (assoc term-definition "@language" (if language (s/lower-case language) nil))))))

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
            (handle-reverse term value local-context defined)    
            ;; 13) - 14) - 15)
            (handle-iri-mapping [term value] local-context defined)
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