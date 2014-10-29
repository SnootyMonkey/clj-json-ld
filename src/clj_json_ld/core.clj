(ns clj-json-ld.core
  (:refer-clojure :exclude [flatten])
  (:require [clj-json-ld.compact :refer (compact-it)]
            [clj-json-ld.expand :refer (expand-it)]
            [clj-json-ld.flatten :refer (flatten-it)]))

(defn compact
  "
  Compacts the given JSON-LD input according to the steps in the
  [JSON-LD Compaction Algorithm](http://www.w3.org/TR/json-ld-api/#compaction-algorithm).

  Compaction is performed using the provided context. If no context is provided, the
  input document is compacted using the top-level context of the document.

  **input** - a JSON-LD document as a Unicode text string, or a string URL to a remote JSON-LD
  document, or a [Map](http://clojure.org/data_structures#Data%20Structures-Maps%20%28IPersistentMap%29)
  representing a parsed JSON-LD document.
  
  **context** *(optional)* - If you want to provide `options` but not a `context`, pass in `nil`
  for the `context`.
  
  **options** *(optional)* - 

  Returns a Clojure [future](https://clojuredocs.org/clojure.core/future) containing the
  compacted JSON-LD that can be read with [deref](https://clojuredocs.org/clojure.core/deref)
  or [@](https://clojuredocs.org/clojure.core/deref).
  "
  ([input] (compact input nil))
  ([input context] (compact input context {}))
  ([input context options]
    (future (compact-it input context options))))

(defn expand
  "
  Expands the given JSON-LD input according to the steps in the
  [JSON-LD Expansion Algorithm](http://www.w3.org/TR/json-ld-api/#expansion-algorithm).

  **input** - a JSON-LD document as a Unicode text string, or a string URL to a remote JSON-LD
  document, or a [Map](http://clojure.org/data_structures#Data%20Structures-Maps%20%28IPersistentMap%29)
  representing a parsed JSON-LD document.
  
  **options** *(optional)* - 

  Returns a Clojure [future](https://clojuredocs.org/clojure.core/future) containing the
  expanded JSON-LD that can be read with [deref](https://clojuredocs.org/clojure.core/deref)
  or [@](https://clojuredocs.org/clojure.core/deref).
  "
  ([input] (expand input {}))
  ([input options]
    (future (expand-it input options))))

(defn flatten
  "
  Flattens and compacts the given JSON-LD input according to the steps in the
  [JSON-LD Flattening Algorithm](http://www.w3.org/TR/json-ld-api/#flattening-algorithm).

  **input** - a JSON-LD document as a Unicode text string, or a string URL to a remote JSON-LD
  document, or a [Map](http://clojure.org/data_structures#Data%20Structures-Maps%20%28IPersistentMap%29)
  representing a parsed JSON-LD document.
  
  **context** *(optional)* - If you want to provide `options` but not a `context`, pass in `nil`
  for the `context`.
  
  **options** *(optional)* - 

  Returns a Clojure [future](https://clojuredocs.org/clojure.core/future) containing the
  compacted JSON-LD that can be read with [deref](https://clojuredocs.org/clojure.core/deref)
  or [@](https://clojuredocs.org/clojure.core/deref).
  "
  ([input] (flatten input nil))
  ([input context] (flatten input context {}))
  ([input context options]
    (future (flatten-it input context options))))