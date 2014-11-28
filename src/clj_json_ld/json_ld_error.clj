(ns clj-json-ld.json-ld-error
  "Aborting errors detected during processing.")

;; TODO Use a real Java exception here, not ex-info
(defn json-ld-error [code message]
  (throw (ex-info "JSONLDError" {:code code :message message})))