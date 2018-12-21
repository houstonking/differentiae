(ns differentiae.progress.change-batch)

(defrecord ChangeBatch [updates clean])

(defn update [change-batch k v]
  (update change-batch :updates conj [k v]))

(defn change-batch
  ([] (->ChangeBatch [] 0))
  ([k v] (update (change-batch) k v)))

(defn dirty? [change-batch]
  (> (count (:updates change-batch))
     (:clean change-batch)))

(defn extend [change-batch coll]
  (reduce update change-batch coll))

(defn into-inner [change-batch]
  (throw (ex-info "NYI")))

(defn drain! [cb-atom] )

(defn drain-into! [cb-atom-1 cb-atom-2])
