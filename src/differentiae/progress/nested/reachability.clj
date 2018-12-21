(ns differentiae.progress.nested.reachability
  (:require [differentiae.progress.frontier :as ft]
            [differentiae.progress.nested.subgraph :as sg])
  (:import [java.util.concurrent LinkedBlockingDeque]))

(def conjv (fnil conj []))

(defrecord Builder
    [nodes edges shape])

(defn builder []
  (map->Builder {:nodes (sorted-map) :edges (sorted-map) :shape (sorted-map)}))


(defn add-node [builder index num-inputs num-outputs summary]
  (-> builder
      (assoc-in [:nodes index] summary)
      (assoc-in [:shape index] [num-inputs num-outputs])))

(defn add-edge [builder source target]
  (update-in builder [:edges (:index source) (:port source)] conjv target))

(defrecord Summary [source->target
                    target->target])

;; TODO: this should be replaced by some sort of default on the timestamp type.
(defn default-timestamp [] 0)

(defn add-summary [v ;; vector of target, antichain pairs
                   target summary]
  ()
  )

(defn summarize [builder]
  (let [work (LinkedBlockingDeque.)]
    (doseq [[index ports] (:edges builder)
            [port targets] ports
            target targets]
      (.addLast work [(sg/source index port) target (default-timestamp)]))
    (let [source->target (atom {})
          target->target (atom {})]
      (doseq [[index [inputs outputs]] (:shape builder)]
        (swap! source->target assoc index (vector (repeat outputs [])))
        (swap! target->target assoc index (vector (repeat inputs []))))

      (while (not-empty work)
        (let [[source target summary] (.pop work)]
          (println source target summary)
          ))
      work
      )))

(defrecord Tracker [sources
                    targets
                    pusheds
                    source-target
                    target-target])

(defn tracker [summary])

(defn update-target [tracker target time value]
  (-> (get-in tracker [:targets (:index target) (:port target)])
      (ft/update-dirty! time value))
  tracker)

(defn update-source [tracker source time value]
  (-> (get-in tracker [:sources (:index source) (:port source)])
      (ft/update-dirty! time value))
  tracker)

(defn node-state [tracker index]
  [(get-in tracker [:targets index])
   (get-in tracker [:sources index])
   (get-in tracker [:pusheds index])])

