(ns differentiae.progress.change-batch
  (:refer-clojure :exclude [update]))

(defn update [change-batch value delta]    
  (merge-with + change-batch {value delta}))

(defn change-batch
  ([] (->ChangeBatch {}))
  ([value delta] (update! (change-batch) {value delta})))

(defn dirty? [change-batch]  
  (> (count (:updates change-batch))
     (:clean change-batch)))

(defn extend! [change-batch coll]
  (reduce (fn [cb [k v]] (update! cb k v)) change-batch coll))

(defn into-inner [change-batch]
  (throw (ex-info "NYI")))

(defn compact [{:keys [updates clean]}]
  (->> (sort-by first updates)
       )
  )

(defn drain! [change-batch]
  (let [[pre post] (swap-vals! (:state change-batch)
                               (constantly base-state))
        {:keys [updates]} pre]
    (compact pre)))

(defn drain-into! [cb-atom-1 cb-atom-2])
