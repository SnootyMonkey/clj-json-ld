(ns clj-json-ld.iri
  (:require [clj.string :refer (blank?)]
            [clj-json-ld.json-ld :refer (json-ld-keyword?)]))
            
;; Internationalized Resource Identifier (IRI)
;; http://en.wikipedia.org/wiki/Internationalized_resource_identifier
;; RFC 3987 http://tools.ietf.org/html/rfc3987

(defun- expand-it 
  
  ; 1) If value is a keyword or null, return value as is.
  ([args :guard #(json-ld-keyword? (:value %))] (:value args))
  ([args :guard #(blank? (:value %))] (:value args))
  
  ; 2) If local context is not null, it contains a key that equals value, and the value
  ; associated with the key that equals value in defined is not true, invoke the
  ; Create Term Definition algorithm, passing active context, local context, value as term,
  ; and defined.

  ; 3) If vocab is true and the active context has a term definition for value, return the associated IRI mapping.

  ; 4) If value contains a colon (:), it is either an absolute IRI, a compact IRI, or a blank node identifier

  ; 5) If vocab is true, and active context has a vocabulary mapping, return the result of concatenating the vocabulary mapping with value.

  ; 6) Otherwise, if document relative is true, set value to the result of resolving value against the base IRI.
  ; Only the basic algorithm in section 5.2 of [RFC3986] is used; neither Syntax-Based Normalization nor
  ; Scheme-Based Normalization are performed. Characters additionally allowed in IRI references are treated in the
  ; same way that unreserved characters are treated in URI references, per section 6.5 of [RFC3987].
  ([args :guard #(:document-relative (:options args))] )

  ; 7) Return value as is.
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

  **active-content** -
  
  **value** - value to be expanded
  
  **options** *(optional)* - 
    
    * **:document-relative** - boolean to indicate if the value can be interpreted as a relative IRI against the documents base IRI, defaults to false
    * **:vocab** - boolean to indicated if the value can be interpreted as a relative IRI against the active context's vocab, defaults to false
    * **:local-context** - defaults to nil
    * **:defined** - map defaults to nil
  "
  ([active-context value] (expand-iri active-context value {}))
  ([active-context value options] 
    (expand-it {:active-context active-context :value value :options options})))

;; http://www.w3.org/TR/json-ld-api/#iri-compaction
;; compact