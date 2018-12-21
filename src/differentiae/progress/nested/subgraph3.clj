(ns differentiae.progress.nested.subgraph3
  (:require [differentiae.progress.frontier :as f]
            [differentiae.progress.operate :as op]
            [differentiae.progress.location :as location]
            [differentiae.progress.change-batch :as cb]
            [differentiae.progress.nested.reachability :as rch]))










(defn- update-local-pointstamps!
  [{:as subgraph :keys [inputs input-messages local-pointstamps children]}]
  (doseq [input (range inputs)]
    (let [input-msgs (nth input-messages input)]
      (doseq [[time delta] (cb/drain! input-msgs)]
        (doseq [target (get-in children [0 :edges input])]
          (swap! local-pointstamps cb/update (location/from target) time delta))
        (swap! local-pointstamps cb/update (location/new-source 0 input) time (- delta)))))
  subgraph)
  
(defn- exchange-progress-information! [{:as subgraph :keys [progcaster local-pointstamps]}]
  ;; TODO:
  ;; self.progcaster.send_and_recv(&mut self.local_pointstamp);
  subgraph)

(defn- update-consumed! [progress-atom port timestamp delta])
(defn- update-produced! [progress-atom port timestamp delta])

(defn- drain-local-pointstamps
  [{:as subgraph :keys [local-pointstamps final-pointstamps]}
   progress-atom]
  (cb/drain-into! final-pointstamps local-pointstamps)
  (doseq [[[location timestamp] delta] (cb/drain! final-pointstamps)]    
    (if (= 0 (:node location))
      (condp (:type location)
          :source (update-consumed! progress-atom (:port location) timestamp delta)
          :target (update-produced! progress-atom (:port location) timestamp delta))
      ))
  
  subgraph)

(defn- propagate-pointstamps [subgraph])
(defn- step-operators! [subgraph])
(defn- report-capabilities [subgraph])

(defrecord Subgraph [inputs
                     input-messages
                     local-pointstamps
                     progcaster]
  op/Operate
  (pull-internal-progress! [this progress-atom]
    (-> this
        update-local-pointstamps!
        exchange-progress-information!
        drain-local-pointstamps
        propagate-pointstamps
        step-operators!
        report-capabilities
        )))
