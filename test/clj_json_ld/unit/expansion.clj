(ns clj-json-ld.unit.expansion
  "Additional unit tests for expansion beyond what's provided in the spec."
  (:require [midje.sweet :refer :all]
            [cheshire.core :refer (parse-string)]
            [clj-json-ld.core :as json-ld]))

(def simple-list-jsonld {"http://example.org/list2" {"@list" [{"@value" nil}]}})
(def simple-list-jsonld2 {"@context" {"mylist2" {"@id" "http://example.com/mylist2" "@container" "@list"}} "mylist2" "one item"})

(facts "list expansion works per the spec"
  (parse-string (json-ld/expand simple-list-jsonld)) => [{"http://example.org/list2" [{"@list" []}]}]
  (parse-string (json-ld/expand simple-list-jsonld2)) => [{"http://example.com/mylist2" [{"@list" [{"@value" "one item"}]}]}])