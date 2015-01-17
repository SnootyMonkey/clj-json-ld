(ns clj-json-ld.expand
  "
  Implement the Expansion operation as defined in the
  [Expansion Algorithm section](http://www.w3.org/TR/json-ld-api/#expansion-algorithm).
  "
  (:require [defun :refer (defun-)]
            [clojure.string :as s]
            [clj-json-ld.util.format :refer (ingest-input format-output)]
            [clj-json-ld.value :refer (expand-value)]
            [clj-json-ld.json-ld-error :refer (json-ld-error)]
            [clj-json-ld.context :refer (update-with-local-context)]
            [clj-json-ld.iri :refer (expand-iri)]
            [clj-json-ld.json-ld :refer (json-ld-keyword?)]))

(defn- drop-key? 
  "
  7.3) If expanded property is null or it neither contains a colon (:) nor it is a keyword, drop key by
  continuing to the next key."
  [key]
  (or
    (nil? key)
    (and
      (not (re-find #":" key))
      (not (json-ld-keyword? key)))))

(defn- string-or-sequence-of-strings?
  "true if the value is either a string or a sequence containing only strings."
  [value]
  (or (string? value) (and (sequential? value) (every? string? value))))

(defn- scalar? [element]
  (or (string? element) (number? element) (true? element) (false? element)))

(defn- type-as-array [key values]
  (let [value (get values key)]
    (if (and (= key "@type") (not (sequential? value))) [value] value)))

(defun- as-sequence
  "Fore a value to be sequential if it's not already, a nil value is an empty sequence."
  ([result :guard nil?] [])
  ([result :guard sequential?] result)
  ([result] [result]))

(declare expansion)
(defun- expand-property

  ;; 7.4.1) If active property equals @reverse, an invalid reverse property map error has been detected and
  ;; processing is aborted.
  ([active-context active-property :guard #(= % "@reverse") expanded-property-value :guard #(json-ld-keyword? (first %))]
    (json-ld-error "invalid reverse property map"
      (str "The active property is @reverse and " (first expanded-property-value) " is a JSON-LD keyword.")))

  ;; 7.4.3) If expanded property is @id and value is not a string, an invalid @id value error has been detected
  ;; and processing is aborted...
  ([active-context active-property expanded-property-value :guard #(and (= (first %) "@id") (not (string? (last %))))]
    (json-ld-error "invalid @id" (str "The value " (last expanded-property-value) " is not a valid @id.")))
  ;; ...Otherwise, set expanded value to the result of using the IRI Expansion algorithm, passing active context,
  ;; value, and true for document relative.
  ([active-context active-property expanded-property-value :guard #(= (first %) "@id")]
    (expand-iri active-context (last expanded-property-value) {:document-relative true}))

  ;; 7.4.4) If expanded property is @type and value is neither a string nor an array of strings, an invalid
  ;; @type value error has been detected and processing is aborted... 
  ([active-context active-property 
      expanded-property-value :guard #(and (= (first %) "@type") (not (string-or-sequence-of-strings? (last %))))]
    (json-ld-error "invalid @type value" (str "The value " (last expanded-property-value) " is not a valid @type.")))
  ;; ...Otherwise, set expanded value to the result of using the IRI Expansion algorithm, passing active context, 
  ;; true for vocab, and true for document relative to expand the value or each of its items.
  ([active-context active-property expanded-property-value :guard #(= (first %) "@type")]
    (let [value (last expanded-property-value)]
      (if (sequential? value)
        (map #(expand-iri active-context % {:document-relative true :vocab true}) value)
        (expand-iri active-context value {:document-relative true :vocab true}))))

  ;; 7.4.6) If expanded property is @value and value is not a scalar or null, an invalid value object value error
  ;; has been detected and processing is aborted...
  ([active-context active-property expanded-property-value :guard #(and (= (first %) "@value") (not (or (scalar? (last %)) (nil? (last %)))))]
    (json-ld-error "invalid value object value" (str "The value " (last expanded-property-value) " is not a valid @value.")))
  ;; ... Otherwise, set expanded value to value. If expanded value is null, set the @value member of result to null and continue with the next key from element. 
  ;; Null values need to be preserved in this case as the meaning of an @type member depends on the existence of an @value member.
  ([active-context active-property expanded-property-value :guard #(= (first %) "@value")]
    (let [value (last expanded-property-value)]
      (if (nil? value)
        {"@value" nil}
        value)))

  ;; 7.4.7) If expanded property is @language and value is not a string, an invalid language-tagged string error
  ;; has been detected and processing is aborted...
  ([active-context active-property expanded-property-value :guard #(and (= (first %) "@language") (not (string? (last %))))]
    (json-ld-error "invalid language-tagged string" (str "The value " (last expanded-property-value) " is not a valid @language string.")))
  ;; ...Otherwise, set expanded value to lowercased value.
  ([active-context active-property expanded-property-value :guard #(= (first %) "@language")]
    (s/lower-case (last expanded-property-value)))

  ;; 7.4.8) error
  ;; 7.4.11) error

    ;; return nil for:
    ;; 7.4.9.1) If active property is null or @graph, continue with the next key from element to remove the 
    ;; free-floating list.
    ;; -or-
    ;; ...Otherwise, set expanded value to the result of using the IRI Expansion algorithm,
    ;; passing active context, value, and true for document relative.
    ;; -or-
    ;; 7.4.5) If expanded property is @graph, set expanded value to the result of using this algorithm recursively
    ;; passing active context, @graph for active property, and value for element.
    ;; -or-
    ;; 7.4.9.2) Otherwise, initialize expanded value to the result of using this algorithm recursively passing active
    ;; context, active property, and value for element.
    ;; -or-
    ;; 7.4.10) If expanded property is @set, set expanded value to the result of using this algorithm recursively, 
    ;; passing active context, active property, and value for element.
    ;; -or-
    ;; 7.4.11.1) Initialize expanded value to the result of using this algorithm recursively, passing active context,
    ;; @reverse as active property, and value as element.

      ;; 7.4.9.3) If expanded value is a list object, a list of lists error has been detected and processing is aborted.

      ;; 7.4.12) Unless expanded value is null, set the expanded property member of result to expanded value.
  ;; or not 7.4)
  ([active-context active-property expanded-property-value]
  ;; 7.5
  ;; or
  ;; 7.6
  ;; or
    ;; 7.7 Otherwise, initialize expanded value to the result of using this algorithm recursively, passing active context, key for active property, and value for element.
    (expand-to-array active-context (first expanded-property-value) (last expanded-property-value))
  ;; +
  ;; 7.8
  ;; 7.9
  ;; 7.10
  ;; 7.11
  ))

(defun- expansion

  ;; 1) If element is null, return null.
  ([_ _ nil] nil)
  
  ;; 2) If element is a scalar,...
  ([active-context active-property element :guard #(scalar? %)]

    ;; 2.1) If active property is null or @graph,...
    (if (or (= active-property nil) (= active-property "@graph"))
      ;; ...drop the free-floating scalar by returning null.
      nil
      ;; 2.2) Return the result of the Value Expansion algorithm, passing the active context, active property, 
      ;; and element as value.
      (expand-value active-context active-property element)))

  ;; 3) If element is an array, ...
  ([active-context active-property element :guard #(sequential? %)]

    ;; 3.1) Initialize an empty array, result.
    ;; 3.2) For each item in element:
    ;; 3.2.1) Initialize expanded item to the result of using this algorithm recursively, passing active context,
    ;; active property, and item as element.
    (let [expanded-elements (map #(expansion active-context active-property %) element)]

      ;; 3.2.2) If the active property is @list or its container mapping is set to @list, ...
      (if (or (= active-property "@list") (= (get active-property "@container") "@list"))
        (doseq [expanded-element expanded-elements]
          ;; ... the expanded item must not be an array or a list object,...
          (if (or (sequential? expanded-element) (contains? expanded-element "@list"))
            ;; ...otherwise a list of lists error has been detected and processing is aborted.
            (json-ld-error "list of lists" "A list of lists was encountered during expansion."))))
      
        ;; 3.2.3) If expanded item is an array, append each of its items to result. Otherwise, if expanded item is
        ;; not null, append it to result.
        ;; 3.3 Return result.
        (filter #(not (nil? %)) (flatten expanded-elements))))

  ;; 5) If element contains the key @context, set active context to the result of the Context Processing algorithm,
  ;; passing active context and the value of the @context key as local context.
  ([active-context active-property element :guard #(and (associative? %) (contains? % "@context"))]
    (expansion 
      (update-with-local-context active-context (get element "@context"))
      active-property
      (dissoc element "@context")))

  ;; 4) Otherwise element is a JSON object.
  ([active-context active-property element]
    
    (let [
      ;; 7) For each key and value in element, ordered lexicographically by key: 
      element-keys (sort (keys element))

      ;; 7.2) Set expanded property to the result of using the IRI Expansion algorithm, passing active context,
      ;; key for value, and true for vocab.
      expanded-keys (map #(expand-iri active-context % {:vocab true}) element-keys)
      ;; 7.4.12) Unless expanded value is null, set the expanded property member of result to expanded value.
      ;; filter out null values
      values (map #(expand-property active-context % [% (get element %)]) element-keys)
      expanded-key-value-map (zipmap expanded-keys values)

      ;; 7.3) If expanded property is null or it neither contains a colon (:) nor it is a keyword, drop key by
      ;; continuing to the next key.
      ;; we have drop-key? as a function for filter for this

      ;; TODO null filtering part of this
      ;; 7.4.12) Unless expanded value is null, set the expanded property member of result to expanded value.
      ;; filter out null values      
      ]

      ;; 7.4.2) If result has already an expanded property member, an colliding keywords error has been detected
      ;; and processing is aborted.

      (zipmap expanded-keys values))))

(defn- expand-to-array [active-context active-property element]
  (let [result (expansion active-context active-property element)]
    (as-sequence result)))

(defn expand-it [input options]
  ;; TODO
  ;; If, after the above algorithm is run, the result is a JSON object that contains only an @graph key, set the result to the value of @graph's value. 
  ;; TODO
  ;; Otherwise, if the result is null, set it to an empty array. Finally, if the result is not an array, then set the result to an array containing only the result.
  ;; Finally, if the result is not an array, then set the result to an array containing only the result.
  (let [result (expansion nil nil (ingest-input input options))
        ;; 8-13 detect some error conditions and tidy up the result
        ;; 8)
        ;; 9) Otherwise, if result contains the key @type and its associated value is not an array, set it to an array containing only the associated value.
        type-array-result (zipmap (keys result) (map #(type-as-array % result) (keys result)))
        ;; 10)
        ;; 11)
        
        ;; 12) If active property is null or @graph, drop free-floating values as follows:
        ;; 12.1) If result is an empty JSON object or contains the keys @value or @list, set result to null.
        
        ;; 12.2) Otherwise, if result is a JSON object whose only key is @id, set result to null.
        final-result (if (and (= (count type-array-result) 1) (contains? type-array-result "@id")) nil type-array-result)]
      ;; 13) Return result
      (format-output (as-sequence final-result) options)))