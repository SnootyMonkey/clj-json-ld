(ns clj-json-ld.context
  "
  Implement the context processing as defined in the
  [Context Processing Algorithms section](http://www.w3.org/TR/json-ld-api/#context-processing-algorithm).
  "
  (:require [defun :refer (defun defun-)]
            [clojure.core.match :refer (match)]
            [clojure.string :as s]
            [clojurewerkz.urly.core :refer (resolve)]
            [clj-json-ld.iri :refer (blank-node-identifier? absolute-iri?)]
            [clj-json-ld.term-definition :refer (create-term-definition)]
            [clj-json-ld.json-ld-error :refer (json-ld-error)]))

;; 3.4) If context has an @base key and remote contexts is empty,
;; i.e., the currently being processed context is not a remote context: 
(defun- process-base-key 

  ; currently being processed context IS a remote context, so do nothing
  ([result context remote-contexts :guard #(not (empty? %))]
    result) 

  ; currently being processed context has a @base key
  ([result context :guard #(contains? % "@base") remote-contexts]
    
    ;; 3.4.1) Initialize value to the value associated with the @base key.
    (let [value (get context "@base")]
      
      (match [value]
        
        ;; 3.4.2) If value is null, remove the base IRI of result.
        [value :guard #(not %)] (dissoc result "@base")
        
        ;; 3.4.3) Otherwise, if value is an absolute IRI, the base IRI of result is set to value.
        [value :guard absolute-iri?] (assoc result "@base" value)

        ;; 3.4.4) Otherwise, if value is a relative IRI and the base IRI of result is not null,
        ;; set the base IRI of result to the result of resolving value against the current base IRI
        ;; of result.
        [value :guard #(and (string? %) (not (s/blank? (get result "@base"))))]
          (assoc result "@base" (resolve (get result "@base") value))

        ;; 3.4.5) Otherwise, an invalid base IRI error has been detected and processing is aborted.
        [_] (json-ld-error "invalid base IRI"
              "local context @base has a relative IRI, and there is no absolute @base IRI in the active context"))))

  ; context has no @base key, so do nothing
  ([result _ _] result))

;; 3.5) If context has an @vocab key: 
(defun- process-vocab-key
  ; currently being processed context has a @vocab key
  ([result context :guard #(contains? % "@vocab")]
    ;; 3.5.1) Initialize value to the value associated with the @vocab key.
    (if-let [vocab (get context "@vocab")]
      ;; 3.5.3) Otherwise, if value is an absolute IRI or blank node identifier,
      ;; the vocabulary mapping of result is set to value. If it is not an absolute
      ;; IRI or blank node identifier, an invalid vocab mapping error has been detected
      ;; and processing is aborted.
      (if (and
            (string? vocab)
            (or (blank-node-identifier? vocab) (absolute-iri? vocab)))
        (assoc result "@vocab" vocab)
        (json-ld-error "invalid vocab mapping"
          "local context has @vocab but it's not an absolute IRI or a blank node identifier"))
      ;; 3.5.2) If value is null, remove any vocabulary mapping from result.
      (dissoc result "@vocab")))
  ; context has no @vocab key, so do nothing
  ([result _] result))


;; 3.6) If context has an @language key: 
(defun- process-language-key
  ; currently being processed context has a @language key
  ([result context :guard #(contains? % "@language")]
    ;; 3.6.1) Initialize value to the value associated with the @language key.
    (if-let [language (get context "@language")]
      ;; 3.6.3) Otherwise, if value is string, the default language of result is set to
      ;; lowercased value. If it is not a string, an invalid default language error has
      ;; been detected and processing is aborted.
      (if (string? language)
        (assoc result "@language" (s/lower-case language))
        (json-ld-error "invalid default language"
          "local context has @language but it's not a string"))
      ;; 3.6.2) If value is null, remove any language mapping from result.
      (dissoc result "@language")))
  ; context has no @language key, so do nothing
  ([result _] result))

;; TODO better/easier than this way to filter keys?
(defn- other-keys
  "Return all the keys from a context that are NOT @base, @vocab and @language."
  [context]
  (->> (keys context)
    (filter #(not (or (= "@base" %)(= "@vocab" %)(= "@language" %))))))

(defun- process-other-keys 
  "3.8) For each key-value pair in context where key is not @base, @vocab, 
  or @language, invoke the Create Term Definition algorithm, passing result
  for active context, context for local context, key, and defined."

  ;; no more keys in the context to process, return the updated context
  ([updated-result _ other-keys :guard empty? _]
    updated-result)
  
  ;; process each key in the context
  ([result context other-keys defined] 
    ;; invoke the Create Term Definition algorithm, passing result for active
    ;; context, context for local context, key, and defined.
    (let [key (first other-keys)
          [updated-result updated-defined] (create-term-definition result context key defined)]
      (recur updated-result context (rest other-keys) updated-defined)))
  
  ;; get the initial set of other keys that we'll be processing and recurse
  ([result context] (recur result context (other-keys context) {})))

;; 3.4, 3.5, and 3.6) If context IS a JSON object, process the context.
(defn- process-local-context [result context remote-contexts]
  (-> result
    ;; 3.4) If context has an @base key and remote contexts is empty, i.e., the currently being processed context is not a remote context: 
    (process-base-key context remote-contexts)
    ;; 3.5) If context has an @vocab key: 
    (process-vocab-key context)
    ;; 3.6) If context has an @language key: 
    (process-language-key context)
    ;; 3.8)
    (process-other-keys context)))

(defun update-with-local-context 
  "Update an active context with a local context."
  
  ;; If remote contexts is not passed, it is initialized to an empty array.
  ([active-context local-context]
    (update-with-local-context active-context local-context []))
  
  ;; 2) If local context is not an array, set it to an array containing only local context.
  ([active-context local-context :guard #(not (sequential? %)) remote-contexts]
    (update-with-local-context active-context [local-context] remote-contexts))

  ;; 1) Initialize result to the result of cloning active context.
  ;; Our recursion accumulator starts as a "clone" of the active-context
  ([active-context local-context remote-contexts]
    (update-with-local-context active-context active-context local-context remote-contexts))

  ;; The next two patterns provide the recursion for:
  ;; 3) For each item context in local context

  ;; stop condition of the recursion; return the accumulated result of merging with the local
  ;; context(s) as the new active context
  ([result active-context local-context :guard #(empty? %) remote-contexts]
    result)

  ([result active-context local-context remote-contexts]
    (let [context (first local-context)]
      (match [context]

        ;; 3.1) If context is null, set result to a newly-initialized active context and continue
        ;; with the next context. The base IRI of the active context is set to the IRI of the
        ;; currently being processed document (which might be different from the currently being
        ;; processed context), if available; otherwise to null. If set, the base option of a JSON-LD
        ;; API Implementation overrides the base IRI.
        [nil] 
          (recur {} active-context (rest local-context) remote-contexts)

        ;; 3.2) If context is a string, it's a remote context, retrieve it, parse it and recurse.
        [string-context :guard #(string? %)]
          (do (println "Remote Context! Do good things here.")
            (recur active-context (rest local-context) remote-contexts))

        ;; 3.3) If context is NOT a JSON object, an invalid local context error has been detected and
        ;; processing is aborted.
        [map-context :guard #(not (map? %))] (json-ld-error "invalid local context" "local context is not a JSON object")
  
        ;; 3.4, 3.5, 3.6, and 3.8) If context IS a JSON object, process the context.
        [_] 
          (recur (process-local-context result (first local-context) remote-contexts)
                 (rest local-context)
                 remote-contexts)))))