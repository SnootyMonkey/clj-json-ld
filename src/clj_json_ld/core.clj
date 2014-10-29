(ns clj-json-ld.core)

(def ^:no-doc response (promise))

(defn compact
  "
  Compacts the given JSON-LD input according to the steps in the
  [JSON-LD Compaction Algorithm](http://www.w3.org/TR/json-ld-api/#compaction-algorithm).

  Compaction is performed using the provided context. If no context is provided, the
  input document is compacted using the top-level context of the document.

  **input** - a JSON-LD document as a Unicode text string, or a string URL to a remote JSON-LD
  document, or an implementation of IPersistentMap representing a parsed JSON-LD document.
  
  **context** *(optional)* - If you want to provide `options` but not a `context`, pass in `nil`
  for the `context`.
  
  **options** *(optional)* - 

  Returns a Clojure [promise](https://clojuredocs.org/clojure.core/promise) containing the
  compacted JSON-LD that can be read with [deref](https://clojuredocs.org/clojure.core/deref)
  or [@](https://clojuredocs.org/clojure.core/deref).
  "
  ([input] (compact input nil))
  ([input context] (compact input context {}))
  ([input context options]
    response))
