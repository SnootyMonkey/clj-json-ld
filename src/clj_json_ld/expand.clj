(ns clj-json-ld.expand
  "
  Implement the Expansion operation as defined in the
  [Expansion Algorithm section](http://www.w3.org/TR/json-ld-api/#expansion-algorithm).
  "
  (:require [defun :refer (defun-)]
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

(defun- expand-property

  ;; 7.4.1) If active property equals @reverse, an invalid reverse property map error has been detected and
  ;; processing is aborted.
  ([active-context active-property :guard #(= % "@reverse") expanded-property-value :guard #(json-ld-keyword? (first %))]
    (json-ld-error "invalid reverse property map"
      (str "The active property is @reverse and " (first expanded-property-value) " is a JSON-LD keyword.")))

  ;; 7.4.3) If expanded property is @id and value is not a string, an invalid @id value error has been detected
  ;; and processing is aborted...
  ([active-context active-property expanded-property-value :guard #(and (= (first %) "@id") (not (string? (last %))))]
    (json-ld-error "invalid @id" (str "The value " (last expanded-property-value) " is not a vaid @id.")))
  ;; ...Otherwise, set expanded value to the result of using the IRI Expansion algorithm, passing active context,
  ;; value, and true for document relative.
  ([active-context active-property expanded-property-value :guard #(= (first %) "@id")]
    (expand-iri active-context (last expanded-property-value) {:document-relative true}))

  ;; 7.4.4) If expanded property is @type and value is neither a string nor an array of strings, an invalid
  ;; @type value error has been detected and processing is aborted... 
  ([active-context active-property 
      expanded-property-value :guard #(and (= (first %) "@type") (not (string-or-sequence-of-strings? (last %))))]
    (json-ld-error "invalid @type value" (str "The value " (last expanded-property-value) " is not a vaid @type.")))
  ;; ...Otherwise, set expanded value to the result of using the IRI Expansion algorithm, passing active context, 
  ;; true for vocab, and true for document relative to expand the value or each of its items.
  ([active-context active-property expanded-property-value :guard #(= (first %) "@type")]
    (let [value (last expanded-property-value)
          values (if (sequential? value) value [value])]
      (map #(expand-iri active-context % {:document-relative true :vocab true}) values)))

    ;; 7.4.6) error
    ;; 7.4.7) error
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
  ;; 7.7
  ;; +
  ;; 7.8
  ;; 7.9
  ;; 7.10
  ;; 7.11
    (println "here" expanded-property-value)
    (last expanded-property-value)
  ))

(defun- expansion
  
  ;; 1) If element is null, return null.
  ([_ _ nil] nil)
  
  ;; 2) If element is a scalar,...
  ([active-context active-property element :guard #(number? %)]

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
  ([active-context active-property element :guard #(contains? % "@context")]
    (expansion 
      (update-with-local-context active-context (get element "@context"))
      active-property
      (dissoc element "@context")))

  ;; 4) Otherwise element is a JSON object.
  ([active-context active-property element]
    ;; 7) For each key and value in element, ordered lexicographically by key: 
    ;; 7.2) Set expanded property to the result of using the IRI Expansion algorithm, passing active context,
    ;; key for value, and true for vocab.
    ;; 7.3) If expanded property is null or it neither contains a colon (:) nor it is a keyword, drop key by
    ;; continuing to the next key.
    (let [keys (filter #(not (drop-key? %)) (map #(expand-iri active-context % {:vocab true}) (sort (keys element))))
          values (map #(expand-property active-context active-property [% (get element %)]) keys)]
      (doseq [key keys]
        (println "expand key" key "to" (expand-property active-context active-property [key (get element key)])))


      ;; 7.4.2) If result has already an expanded property member, an colliding keywords error has been detected
      ;; and processing is aborted.

      ;; 7.4.12) Unless expanded value is null, set the expanded property member of result to expanded value.
      ;; filter out null values

      ;; 8-13 detect some error conditions and tidy up the result
      (zipmap keys values))))

(defn expand-it [input options]
  (format-output (expansion nil nil (ingest-input input options)) options))