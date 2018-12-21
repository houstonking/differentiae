(ns differentiae.progress.location)

(defrecord Location [node port type])

(defn from [target]
  (throw (ex-info "NYI"))
  #_(Location. (:index target) ))

(defn new-source [node port]
  (Location. node port :source))
