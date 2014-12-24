(ns clj-json-ld.expand
  "
  Implement the Expansion operation as defined in the
  [Expansion Algorithm section](http://www.w3.org/TR/json-ld-api/#expansion-algorithm).
  "
  (:require [defun :refer (defun-)]
            [clj-json-ld.util.format :refer (ingest-input format-output)]
            [clj-json-ld.value :refer (expand-value)]
            [clj-json-ld.json-ld-error :refer (json-ld-error)]))

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

  ;; 4) Otherwise element is a JSON object.
  ([active-context active-property element] "[]"))

(defn expand-it [input options]
  (format-output (expansion nil nil (ingest-input input options)) options))