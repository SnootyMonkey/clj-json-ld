(ns clj-json-ld.context
  "
  Implement the context processing as defined in the
  [Context Processing Algorithms section](http://www.w3.org/TR/json-ld-api/#context-processing-algorithm).
  "
  (:require [defun :refer (defun)]
            [clojure.core.match :refer (match)]))

(defun update-with-local-context 
  "Update an active context with a local context."
  
  ;; If remote contexts is not passed, it is initialized to an empty array.
  ([active-context local-context]
    (update-with-local-context active-context local-context []))
  
  ;; 2) If local context is not an array, set it to an array containing only local context.
  ([active-context local-context :guard #(not (vector? %)) remote-contexts]
    (update-with-local-context active-context [local-context] remote-contexts))

  ;; The next two patterns provide the recursion for:
  ;; 3) For each item context in local context

  ([active-context [] remote-contexts]
    (println "empty!"))

  ([active-context local-context remote-contexts]
    (let [context (first local-context)]
      (match [context]

        [nil] 
          ;; 3.1) If context is null, set result to a newly-initialized active context and continue
          ;; with the next context. The base IRI of the active context is set to the IRI of the
          ;; currently being processed document (which might be different from the currently being
          ;; processed context), if available; otherwise to null. If set, the base option of a JSON-LD
          ;; API Implementation overrides the base IRI.
          (println "Nothing!")

        [string-context :guard #(string? %)]
          ;; 3.2) If context is a string
          (println "String!")

        [map-context :guard #(not (map? %))] 
          ;; TODO Use a real Java exception here, not ex-info
          ;; 3.3) If context is NOT a JSON object, an invalid local context error has been detected and
          ;; processing is aborted.
          (throw (ex-info "JSONLDError" {:code "invalid local context"
            :message "local context is not a JSON object"}))

        [_] 
          ;; 3.4, 3.5, 3.6) If context IS a JSON object, process the context
          (println "Context processing!"))
        ;; If context has an @base key and remote contexts is empty, i.e., the currently being processed context is not a remote context: 

    (recur active-context (vec (rest local-context)) remote-contexts))))