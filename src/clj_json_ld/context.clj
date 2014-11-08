(ns clj-json-ld.context
  "
  Implement context processing as defined in the
  [Context Processing Algorithms section](http://www.w3.org/TR/json-ld-api/#context-processing-algorithm).
  "
  (:require [defun :refer (defun)]))

(defun update-with-local-context 
  "Update an active context with a local context."
  
  ;; If remote contexts is not passed, it is initialized to an empty array.
  ([active-context local-context]
    (update-with-local-context active-context local-context []))
  
  ;; 2) If local context is not an array, set it to an array containing only local context.
  ([active-context local-context :guard #(not (vector? %)) remote-contexts]
    (update-with-local-context active-context [local-context] remote-contexts))

  ;; Recursion for:
  ;; 3) For each item context in local context

  ([active-context [] remote-contexts]
    (println "empty!"))

  ([active-context local-context remote-contexts]
    (println "NOT empty!" (first local-context))
    (update-with-local-context active-context (vec (rest local-context)) remote-contexts)))