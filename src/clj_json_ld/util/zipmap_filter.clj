(ns clj-json-ld.util.zipmap-filter
  (:require [defun :refer (defun)]))

; Falklandsophile begat FCMS begat clj-json-ld begat zipmap-filter

; New library project
; New GH repo
; Move the namespace
; Create the tests
; Add direct filter functions
; Generate API docs - maybe use https://github.com/gdeer81/marginalia like http://atroche.github.io/clj-sockets/
; Create the tests
; Add core.typed support
; Readme
; CI on travis
; submit to clojars
; add to SM.com
; submit to clojure mailing list

; Presentation content:

; create a lib
;; This is zipmap
; (defn zipmap
;   "Returns a map with the keys mapped to the corresponding vals."
;   [keys vals]
;   (loop [map {}
;          ks (seq keys)
;          vs (seq vals)]
;     (if (and ks vs)
;       (recur (assoc map (first ks) (first vs))
;              (next ks)
;              (next vs))
;       map)))

;; This is zipmap on patterns
; (defun zipmap
;   "Returns a map with the keys mapped to the corresponding vals."
;   ([ks vs] (recur {} (seq ks) (seq vs)))
;   ([map ks :guard empty? vs] map)
;   ([map ks vs :guard empty?] map)
;   ([map ks vs] (recur (assoc map (first ks) (first vs)) (next ks) (next vs))))

; Show them zipmap-filter

;; Any questions?

(defun zipmap-filter
  "Returns a map with the keys mapped to the corresponding values for the keys and values that satisfy the predicate."
  ([pred :guard #(not (associative? %)) ks vs] (recur {} [pred (seq ks)] [pred (seq vs)]))
  ([key-pred value-pred ks vs] (recur {} [key-pred (seq ks)] [value-pred (seq vs)]))
  ([map ks-tuple :guard #(empty? (last %)) vs-tuple] map)
  ([map ks-tuple vs-tuple :guard #(empty? (last %))] map)
  ([map ks-tuple :guard #(not ((first %) (first (last %)))) vs-tuple] (recur map [(first ks-tuple) (next (last ks-tuple))] [(first vs-tuple) (next (last vs-tuple))]))
  ([map ks-tuple vs-tuple :guard #(not ((first %) (first (last %))))] (recur map [(first ks-tuple) (next (last ks-tuple))] [(first vs-tuple) (next (last vs-tuple))]))
  ([map ks-tuple vs-tuple] (recur (assoc map (first (last ks-tuple)) (first (last vs-tuple))) [(first ks-tuple) (next (last ks-tuple))] [(first vs-tuple) (next (last vs-tuple))])))

(defun zipmap-filter-keys
  "Returns a map with the keys mapped to the corresponding values for the keys that satisfy the predicate."
  ([pred :guard #(not (associative? %)) ks vs] (recur {} [pred (seq ks)] (seq vs)))
  ([map ks-tuple :guard #(empty? (last %)) vs] map)
  ([map ks vs :guard empty?] map)
  ([map ks-tuple :guard #((first %) (first (last %))) vs] (recur (assoc map (first (last ks-tuple)) (first vs)) [(first ks-tuple) (next (last ks-tuple))] (next vs)))
  ([map ks-tuple vs] (recur map [(first ks-tuple) (next (last ks-tuple))] (next vs))))

(defun zipmap-filter-values
  "Returns a map with the keys mapped to the corresponding values for the values that satisfy the predicate."
  ([pred :guard #(not (associative? %)) ks vs] (recur {} (seq ks) [pred (seq vs)]))
  ([map ks :guard empty? vs-tuple] map)
  ([map ks vs-tuple :guard #(empty? (last %))] map)
  ([map ks vs-tuple :guard #((first %) (first (last %)))] (recur (assoc map (first ks) (first (last vs-tuple))) (next ks) [(first vs-tuple) (next (last vs-tuple))]))
  ([map ks vs-tuple] (recur map (next ks) [(first vs-tuple) (next (last vs-tuple))])))