(ns differentiae.progress.location)

(defrecord Location [node port type])

(defn from [target]
  (throw (ex-info "NYI"))
  #_(Location. (:index target) ))

(defn source [node port]
  (Location. node port :source))

(defn source? [loc] (identical? :source (:type loc)))

(defn target [node port]
  (Location. node port :target))

(defn target? [loc] (identical? :target (:type loc)))
