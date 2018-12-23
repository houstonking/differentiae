(ns differentiae.progress.location)

(defrecord Location [node port type])

(defn from [target]
  (throw (ex-info "NYI"))
  #_(Location. (:index target) ))

(defn source [node port]
  (Location. node port :source))

(defn target [node port]
  (Location. node port :target))
