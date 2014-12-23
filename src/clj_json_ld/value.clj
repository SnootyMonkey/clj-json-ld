(ns clj-json-ld.value
  "
  Implement value expansion as defined in the
  [Value Expansion section](http://www.w3.org/TR/json-ld-api/#value-expansion).

  Implement value compaction as defined in the
  [Value Compaction section](http://www.w3.org/TR/json-ld-api/#value-compaction).
  "
  (:require [defun :refer (defun-)]
            [clj-json-ld.iri :refer (expand-iri)]))

(defn- property-type-mapping
  "If active property has a type mapping in active context, return it if it's not @id."
  [active-context active-property]
  (let [type-mapping (get-in active-context [active-property "@type"])]
    (if (= type-mapping "@id") nil type-mapping)))

(defn- value-is-an-iri?
  "Check the active context for a type mapping for the active property, and return true
  if the type is an IRI, and false if it is not."
  [args]
    (= (get-in (:active-context args) [(:active-property args) "@type"]) "@id"))

(defun- language-mapping
  ;;  If a language mapping is associated with active property in active context, add
  ;;  an @language to result and set its value to the language code associated with the
  ;;  language mapping; unless the language mapping is set to null in which case no member
  ;;  is added.
  ([args :guard #(get-in % [:active-context (get % :active-property) "@language"])]
    (assoc (:value-map args) "@language" (get-in args [:active-context (get args :active-property) "@language"])))

  ;; Otherwise, if the active context has a default language, add an @language
  ;;  to result and set its value to the default language.
  ([args :guard #(get-in % [:active-context "@language"])]
    (assoc (:value-map args) "@language" (get-in args [:active-context "@language"])))

  ;; Otherwise, there is no language mapping for you! Good luck guessing the language sucker!
  ([args] (:value-map args)))

(defun- expand-it
  ;; 1) If the active property has a type mapping in active context that is @id,
  ;; return a new JSON object containing a single key-value pair where the key is @id
  ;; and the value is the result of using the IRI Expansion algorithm, passing active
  ;; context, value, and true for document relative.
  ([args :guard #(value-is-an-iri? %)]
    {"@id" (expand-iri (:active-context args) (:value args) {:document-relative true})})

  ;; 2) If active property has a type mapping in active context that is @vocab, return a new JSON object containing a single key-value pair where the key is @id and the value is the result of using the IRI Expansion algorithm, passing active context, value, true for vocab, and true for document relative.

  ;; 3) Otherwise, initialize result to a JSON object with an @value member whose value is set to value.
  ([args] {"@value" (:value args)}))

(defn expand-value
  "
  [Value Expansion](http://www.w3.org/TR/json-ld-api/#value-expansion)

  Some values in JSON-LD can be expressed in a compact form. These values are required to
  be expanded at times when processing JSON-LD documents.

  If active property has a type mapping in the active context set to @id or @vocab, a JSON
  object with a single member @id whose value is the result of using the IRI Expansion
  algorithm on value is returned.

  Otherwise, the result will be a JSON object containing an @value member whose value is the
  passed value. Additionally, an @type member will be included if there is a type mapping
  associated with the active property or an @language member if value is a string and there
  is language mapping associated with the active property.

  **active-context** - context map used to resolve terms

  **active-property** - the property whose value is being expanded

  **value** - value to be expanded
  "
  [active-context active-property value]
    ;; Pairtially expand the value and get its type mapping
    (let [partially-expanded-value (expand-it {:active-context active-context
                                               :active-property active-property
                                               :value value})
          type-mapping (property-type-mapping active-context active-property)]

      ;; 4) If active property has a type mapping in active context [and it's not @id],
      ;; add an @type member to result and set its value to the value associated with the
      ;; type mapping.
      (if type-mapping
        (assoc partially-expanded-value "@type" type-mapping)

        ;; 5) Otherwise, if there is an @value, and it's a string, add the (optional)
        ;; language mapping
        (let [value (get partially-expanded-value "@value")]
          (if (and value (string? value))
            ;; Add the (optional) language mapping
            (language-mapping {:active-context active-context
                               :active-property active-property
                               :value-map partially-expanded-value})
            partially-expanded-value))))) ; Non-strings and @id's don't get language mappings