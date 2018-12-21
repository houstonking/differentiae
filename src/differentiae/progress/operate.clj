(ns differentiae.progress.operate
  "Methods for describing an operators topology, and the progress it makes."
  (:require [differentiae.progress.timestamp :as ts]
            [differentiae.progress.change-batch :as cb]
            [differentiae.progress.frontier :as ft]))


(defprotocol Operate
  (pull-internal-progress! [operator progress-atom]))
  
(def frontier :frontiers)
(def consumed :consumeds)
(def internal :internals)
(def produced :produceds)

(defrecord SharedProgress [frontiers
                           consumeds
                           internals
                           produceds])

(defn shared-progress [inputs outputs]
  (map->SharedProgress
   {:frontiers (atom (repeatedly inputs cb/change-batch))
    :consumeds (atom (repeatedly inputs cb/change-batch))
    :internals (atom (repeatedly outputs cb/change-batch))
    :produceds (atom (repeatedly outputs cb/change-batch))}))

