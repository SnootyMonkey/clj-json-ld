(ns clj-json-ld.iri
  "
  Implement IRI expansion as defined in the
  [IRI Expansion section](http://www.w3.org/TR/json-ld-api/#iri-expansion).

  Implement IRI compaction as defined in the
  [IRI Compaction section](http://www.w3.org/TR/json-ld-api/#iri-compaction).
  "
  (:require [clojure.string :refer (blank? split join)]
            [defun :refer (defun-)]
            [clojure.core.match :refer (match)]
            [clj-json-ld.json-ld :refer (json-ld-keyword?)]
            [clojurewerkz.urly.core :as u]))

;; Internationalized Resource Identifier (IRI)
;; http://en.wikipedia.org/wiki/Internationalized_resource_identifier
;; RFC 3987 http://tools.ietf.org/html/rfc3987

;; Example of a compact IRI (foaf:name)
;; {
;;   "@context":
;;   {
;;     "foaf": "http://xmlns.com/foaf/0.1/"
;; ...
;;   },
;;   "@type": "foaf:Person"
;;   "foaf:name": "Dave Longley",
;; ...
;; }

(defn- handle-colon [active-context value local-context]
  ;; 4.1 Split value into a prefix and suffix at the first occurrence of a colon (:).
  (let [parts (split value #":")
        prefix (first parts)
        suffix (join ":" (rest parts))]

    (match [prefix suffix]

      ;; 4.2) If prefix is underscore (_) or suffix begins with double-forward-slash (//), return value as it is
      ;; already an absolute IRI or a blank node identifier.
      ["_" _] value
      [_ (suffix :guard #(re-find #"^//" %))] value

      ;; 4.3) If local context is not null, it contains a key that equals prefix, and the value associated with the
      ;; key that equals prefix in defined is not true, invoke the Create Term Definition algorithm, passing active
      ;; context, local context, prefix as term, and defined. This will ensure that a term definition is created for
      ;; prefix in active context during Context Processing.

      ;; 4.4) If active context contains a term definition for prefix, return the result of concatenating the IRI
      ;; mapping associated with prefix and suffix.
      [(prefix :guard #(get active-context %)) suffix] (str (get active-context prefix) suffix)

      ;; 4.5) Return value as it is already an absolute IRI.
      [_ _] value)))

(defun- expand-it

  ;; 1) If value is a keyword or null, return value as is.
  ([args :guard #(json-ld-keyword? (:value %))] (:value args))
  ([args :guard #(nil? (:value %))] (:value args))

  ;; 2) If local context is not null, it contains a key that equals value, and the value
  ;; associated with the key that equals value in defined is not true, invoke the
  ;; Create Term Definition algorithm, passing active context, local context, value as term,
  ;; and defined.

  ;; 3) If vocab is true and the active context has a term definition for value, return the associated IRI mapping.
  ([args :guard #(and (get-in % [:options :vocab]) (get-in % [:active-context (:value %)]))]
    (get-in args [:active-context (:value args)]))

  ; 4) If value contains a colon (:), it is either an absolute IRI ("http://schema.org/name"), a compact
  ;; IRI ("foaf:name"), or a blank node identifier ("_:")
  ([args :guard #(.contains (:value %) ":")]
    (handle-colon (:active-context args) (:value args) (get-in args [:options :local-context])))

  ;; 5) If vocab is true, and active context has a vocabulary mapping, return the result of concatenating the
  ;; vocabulary mapping with value.
  ([args :guard #(and (get-in % [:options :vocab]) (get-in % [:active-context "@vocab"]))]
    (str (get-in args [:active-context "@vocab"]) (:value args)))

  ;; 6) Otherwise, if document relative is true, set value to the result of resolving value against the base IRI.
  ;; Only the basic algorithm in section 5.2 of [RFC3986] is used; neither Syntax-Based Normalization nor
  ;; Scheme-Based Normalization are performed. Characters additionally allowed in IRI references are treated in the
  ;; same way that unreserved characters are treated in URI references, per section 6.5 of [RFC3987].
  ([args :guard #(:document-relative (:options %))]
    (if-let [base (get-in args [:active-context "@base"])]
      (u/resolve base (:value args))
      (:value args)))

  ;; 7) Return value as is.
  ([args] (:value args)))

(defn expand-iri
  "
  [IRI Expansion](http://www.w3.org/TR/json-ld-api/#iri-expansion)

  In JSON-LD documents, some keys and values may represent IRIs. This is an algorithm for
  transforming a string that represents an IRI into an absolute IRI or blank node
  identifier. It also covers transforming keyword aliases into keywords.

  A blank node identifier is a string that can be used as an identifier for a blank node
  within the scope of a JSON-LD document. Blank node identifiers begin with _:

  IRI expansion may occur during context processing or during any of the other
  JSON-LD algorithms. If IRI expansion occurs during context processing, then the
  local context and its related defined map from the Context Processing algorithm
  are passed to this algorithm. This allows for term definition dependencies to be
  processed via the Create Term Definition algorithm.

  **active-context** - context map used to resolve terms

  **value** - value to be expanded

  **options** *(optional)* -

    * **:document-relative** - true/false the value can be interpreted as a relative IRI against the documents base IRI, defaults to false
    * **:vocab** - true/false the value can be interpreted as a relative IRI against the active context's vocab, defaults to false
    * **:local-context** - defaults to nil
    * **:defined** - map defaults to nil
  "
  ([active-context value] (expand-iri active-context value {}))
  ([active-context value options]
    (expand-it {:active-context active-context :value value :options options})))

(defn absolute-iri?
  "Wrap urly's absolute? in a try because it blows up a lot."
  [string]
  (try
    (u/absolute? string)
    (catch Exception e
      false))) ; it blew up urly's absolute? so... it's not

(defn blank-node-identifier?
  "A blank node identifier is a string that can be used as an identifier for a blank node within the scope of a
  JSON-LD document. Blank node identifiers begin with _:."
  [identifier]
  (if (and (string? identifier) (re-find #"^_:" identifier)) true false))

(defn compact-iri?
  "
  Return a boolean to indicate if the string argument is a compact IRI or not.
  http://www.w3.org/TR/json-ld-api/#dfn-compact-iri
  "
  [string]
  (let [parts (split string #":")
        prefix (first parts)
        suffix (last parts)]
    (and 
      ; have 1 and only 1 colon
      (= (count parts) 2)
      ; not a blank node identifier
      (not (blank-node-identifier? string))
      ; have a prefix and suffix that don't have whitespace
      (not (re-find #"\s" prefix))
      (not (re-find #"\s" suffix))
      ; neither the prefix nor the suffix is blank
      (not (blank? prefix))
      (not (blank? suffix)))))

;; http://www.w3.org/TR/json-ld-api/#iri-compaction
;; compact