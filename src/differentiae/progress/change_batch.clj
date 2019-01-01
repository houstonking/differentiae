(ns differentiae.progress.change-batch
  (:refer-clojure :exclude [update]))

(def empty-change-map (sorted-map))

(defn update! [change-batch value delta]    
  (if (zero? delta)
    change-batch
    (do
      (swap! change-batch (partial merge-with +) {value delta})
      change-batch)))

(defn change-batch
  ([] (atom empty-change-map))
  ([value delta] (update! (change-batch) value delta)))

(defn extend! [change-batch coll]
  (reduce (fn [cb [k v]] (update! cb k v)) change-batch coll))

(defn drain! [change-batch]
  (let [[pre post] (swap-vals! change-batch (constantly empty-change-map))]
    (filter  #(not (zero? (second %))) pre)))


