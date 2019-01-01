(ns differentiae.progress.nested.reachability
  (:require [differentiae.progress.frontier :as ft]
            [differentiae.progress.location :as loc]
            [differentiae.progress.change-batch :as cb]
            [differentiae.progress.nested.subgraph :as sg]
            [differentiae.progress.timestamp :as ts])
  (:import [java.util.concurrent LinkedBlockingDeque]))

;;;;;;;;;;;;;;;;;;;;;;
;; PORT INFORMATION ;;
;;;;;;;;;;;;;;;;;;;;;;

(defrecord PortInformation [pointstamps implications output-summaries])

(defn port-information [] (map->PortInformation {:pointstamps (ft/mutable-antichain)
                                                 :implications (ft/mutable-antichain)}))

;;;;;;;;;;;;;;;;;;
;; PER OPERATOR ;;
;;;;;;;;;;;;;;;;;;

(defrecord PerOperator [targets sources])

(defn per-operator [inputs outputs]
  (map->PerOperator {:targets (vec (repeat inputs (port-information)))
                     :sources (vec (repeat outputs (port-information)))}))

;;;;;;;;;;;;;
;; TRACKER ;;
;;;;;;;;;;;;;

(defrecord Tracker [nodes
                    edges
                    per-operator
                    target-changes
                    source-changes
                    worklist
                    pushed-changes
                    output-changes
                    total-counts])

(defn update-source! [{:as tracker :keys [source-changes]} source time value]
  (cb/update! source-changes [source time] value))


(defn- accumulate-target-changes! [{:as tracker
                                    :keys [target-changes]}]  
  tracker)

(defn- accumulate-source-changes! [{:as tracker
                                    :keys [source-changes]}]  
  (reduce
   (fn [tracker [[source time] diff]]
     (let [operator (-> tracker :per-operator (get (:node source)) :sources (get (:port source)))
           changes (ft/update-iter! (-> operator :pointstamps) [[time diff]])]
       (reduce
        (fn [{:as tracker
              :keys []}
             [time diff]]
          (let [tracker (reduce (fn [{:as tracker :keys [output-changes worklist]} [output summaries]]
                                  (doseq [summary summaries
                                          :let [out-time (ts/results-in summary time)]]
                                    (cb/update! (get output-changes output) out-time diff)))
                                tracker
                                (-> operator :output-summaries enumerate))]

            
            (-> tracker
                (update :total-counts (fnil + 0) diff)
                (update :worklist conj [time source diff]))))
        tracker
        changes)))
   tracker
   (cb/drain! source-changes)))

(defn rcompare [x y] (compare y x))

(defn- propagate-changes! [{:as tracker :keys [worklist]}]  
  (loop [[[time location diff :as work] & cur-work :as all-work] worklist]        
    (when work      
      (if (zero? diff)
        (recur cur-work)
        (if (loc/target? location)                    
          (let [changes (-> tracker
                            :per-operator
                            (get (:node location))
                            :targets
                            (get (:port location))
                            :implications
                            (ft/update-iter! [[time diff]]))                
                nodes (-> tracker :nodes (get (:node location)) (get (:port location)))
                new-work (doall (for [[time diff] changes
                                      [output-port summaries] (-> tracker
                                                                  :nodes
                                                                  (get (:node location))
                                                                  (get (:port location))
                                                                  enumerate)
                                      :let [source (loc/source (:node location) output-port)]
                                      summary (:elements summaries)]                                    
                                  (let [new-time (ts/results-in summary time)]
                                    (update tracker :pushed-changes cb/update! [location time] diff)
                                    [new-time source diff])))]              
            (recur (into (sorted-set) (concat cur-work new-work))))
          (let [changes (-> tracker
                            :per-operator
                            (get (:node location))
                            :sources
                            (get (:port location))
                            :implications
                            (ft/update-iter! [[time diff]]))
                new-work (doall (for [[time diff] changes
                                      target (-> tracker
                                                 :edges
                                                 (get (:node location))
                                                 (get (:port location)))]
                                  (do (update tracker :pushed-changes cb/update! [location time] diff)
                                      [time target diff])))]            
            (recur (into (sorted-set) (concat cur-work new-work))))))))
  tracker)

(defn propagate-all! [tracker]  
  (-> tracker accumulate-target-changes! accumulate-source-changes! propagate-changes!))

(defn drain-pushed! [{:as tracker :keys [pushed-changes]}]
  (cb/drain! pushed-changes))

(defn pushed [{:as tracker :keys [pushed-changes]}]
  pushed-changes)

;;;;;;;;;;;;;;;;;;;;;;;;;;
;; REACHABILITY BUILDER ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord ReachabilityBuilder [;; indexed by operator, then input port, then output port
                                ;; same shape as get_internal_summary
                                nodes 
                                ;; Direct connections from sources to targets
                                ;; indexed by operator, then output port
                                edges
                                ;; Numbers of inputs and outputs for each node
                                shape])

(defn builder []
  (atom (map->ReachabilityBuilder {})))

(defn add-node! [builder index inputs outputs summaries]
  (assert (= inputs (count summaries)))
  (doseq [summary summaries]
    (assert (= outputs (count summary))))
  (swap! builder
         (fn [builder]           
           (-> builder
               (assoc-in [:edges index] (zipmap (range outputs) (repeat outputs [])))
               (assoc-in [:nodes index] summaries)
               (assoc-in [:shape index] [inputs outputs]))))
  builder)

(defn add-edge! [builder source target]  
  (swap! builder (fn [builder]
                   (update-in builder [:edges
                                       (:node source)
                                       (:port source)]
                              conj target)))
  builder)

(defn enumerate [xs] (zipmap (range (count xs)) xs))

(defn summarize-outputs
  ([nodes edges] (summarize-outputs nodes edges Long))
  ([nodes edges timestamp-type]  
   (let [reverse (into {}
                       (for [[node outputs] edges
                             [output targets] outputs
                             target targets]
                         [target (loc/source node output)]))         
         outputs (doall (for [[_ outputs] edges
                              [_ targets] outputs
                              target targets
                              :when (= 0 (:node target))]
                          target))
         init-work (doall (for [output outputs]
                            [output (:port output) 0 #_ts/default-summary]))]      ; TODO: see if we can come up with a better pattern for default timestamps wrt types
     (loop [results {}
            [work & work-rest :as all-work] init-work]       
       (if (nil? work)
         results
         (let [[location output summary] work]
           (cond
             (loc/source? location)
             ;; TODO: less nasty translation
             (let [output-port (:port location)
                   [results' work']
                   (reduce
                    (fn [[results work] [input-port summaries]]                      
                      (let [location (loc/target (:node location) input-port)]
                        (reduce
                         (fn [[results work] operator-summary]                                                      
                           (let [combined (ts/followed-by operator-summary summary)                                 
                                 cur-antichains (get results location {})
                                 updated-antichains (update cur-antichains output
                                                            (fnil ft/insert (ft/antichain)) combined)
                                 inserted? (not= cur-antichains updated-antichains)]
                             [(assoc results location updated-antichains)
                              (if inserted?
                                (concat work-rest [[location output combined]])
                                work-rest)]))
                         [results work]
                         (-> summaries (get output-port) :elements))))
                    [results work-rest]
                    (enumerate (get nodes (:node location))))]               
               (recur results' work'))
             (loc/target? location)

             (if-let [source (get reverse location)]              
               (let [cur-antichains (get results source {})
                     updated-antichains (update cur-antichains output
                                                (fnil ft/insert (ft/antichain)) summary)
                     inserted? (not= cur-antichains updated-antichains)]                              
                 (recur (assoc results source updated-antichains)
                        (if inserted?
                          (concat work-rest [[source output summary]])
                          work-rest)))
               (recur results work-rest)))))))))

(defn build! [builder]
  (let [b @builder        
        per-ops (into {} (map (fn [[i [inputs outputs]]]
                                [i (per-operator inputs outputs)])
                              (-> b :shape)))        
        builder-summary (vec (repeat (get-in b [:shape 0 1]) []))
        output-summaries (summarize-outputs (:nodes b) (:edges b))
        [builder-summary per-ops]
        (loop [builder-summary builder-summary
               per-ops per-ops
               [[location summaries :as nxt] & rst] output-summaries]
          (if (nil? nxt)
            [builder-summary per-ops]            
            (if (zero? (:node location))
              (recur (cond-> builder-summary
                       (loc/source? location) (update (:port location) (constantly summaries)))
                     per-ops rst)
              (recur builder-summary
                     (if (loc/target? location)
                       (update-in per-ops [(:node location)
                                           :targets
                                           (:port location)
                                           :output-summaries]
                                  (constantly summaries))
                       (update-in per-ops [(:node location)
                                           :sources
                                           (:port location)
                                           :output-summaries]
                                  (constantly summaries)))                     
                     rst))))        
        scope-outputs (get-in b [:shape 0 0])        
        output-changes (vec (repeat scope-outputs (cb/change-batch)))]    
    [(map->Tracker
      {:nodes (-> b :nodes)
       :edges (-> b :edges)
       :per-operator per-ops
       :target-changes (cb/change-batch)
       :source-changes (cb/change-batch)
       :worklist (sorted-set)
       :pushed-changes (cb/change-batch)
       :total-outputs 0})
     builder-summary]))

