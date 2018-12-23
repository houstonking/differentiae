(ns differentiae.progress.nested.subgraph-builder
  (:require [differentiae.progress.change-batch :as cb]
            [differentiae.progress.frontier :as f]
            [differentiae.progress.operate :as op]
            [differentiae.progress.location :as location]
            [differentiae.progress.nested.operator-state :as operator-state]
            [differentiae.progress.nested.reachability :as reachability]
            [differentiae.progress.broadcast :as broadcast]
            [differentiae.progress.nested.subgraph :as subgraph]            
            [differentiae.scheduling :as scheduling]))

(defrecord SubgraphBuilder [name
                            path
                            index
                            children
                            child-count
                            edge-stash
                            input-messages
                            output-capabilities
                            logging])

(defn new-from [index path logging name]
  (atom (SubgraphBuilder. name
                          (conj path index)
                          index
                          [(operator-state/empty-operator 0 0)]
                          1
                          []
                          []
                          []
                          logging)))

(defn new-input! [subgraph-builder shared-counts]
  (let [after (swap! subgraph-builder update :input-messages conj shared-counts)]
    (location/target (-> after :index)
                     (-> after :input-messages count dec))))

(defn new-output! [subgraph-builder]
  (let [after (swap! subgraph-builder
                     update
                     :output-capabilities
                     conj
                     (f/mutable-antichain))]
    (location/source (-> after :index)
                     (-> after :output-capabilities count dec))))

(defn connect! [subgraph-builder source target]
  (swap! subgraph-builder update :edge-stash conj [source target]))

(defn allocate-child-id! [subgraph-builder]
  (-> (swap! subgraph-builder update :child-count inc)
      :child-count
      dec))

(defn add-child! [subgraph-builder child index identifier]
  (swap! subgraph-builder
         (fn add-child [{:as sb :keys [path logging]}]
           (let [new-child (operator-state/operator-state
                            child index path identifier logging)]
             (update :children conj new-child)))))

(defn base-rbuilder [outputs inputs]
  (-> (reachability/builder)
      (reachability/add-node! 0 outputs inputs
                             (vec (repeat outputs
                                         (vec (repeat inputs (f/antichain))))))))

(defn heap [])

(defn add-node-from-child! [builder index child]
  (reachability/add-node! builder index (:inputs child) (:outputs child) (:internal-summary child)))

(defn build! [subgraph-builder worker]
  (let [{:as sb_
         :keys [name path logging input-messages output-capabilities children edge-stash]}
        @subgraph-builder
        children (sort-by :index children)
        inputs (count input-messages)
        outputs (count output-capabilities)
        self-operator (operator-state/empty-operator outputs inputs)
        rbuilder (base-rbuilder outputs inputs)
        _ (doall (map-indexed
                  (fn [index child]
                    (add-node-from-child! rbuilder index child))
                  (rest children)))
        children' (reduce
                   (fn [children [source target]]
                     (reachability/add-edge! rbuilder source target)
                     (update-in children [(:index source) :edges (:port source)]
                             conj target))
                   children
                   edge-stash)
        [tracker scope-summary] (reachability/build! rbuilder)
        progcaster (broadcast/progcaster worker path logging)]
    (subgraph/map->Subgraph
     {:name name
      :path path
      :inputs inputs
      :outputs outputs
      :incomplete (into [false] (repeat (dec (count children)) true))
      :incomplete-count (dec (count children))
      :activations (:activations worker)
      :temp-active (heap)
      :children children
      :input-messages input-messages
      :output-capabilities output-capabilities
      :local-pointstamp (cb/change-batch)
      :final-pointstamp (cb/change-batch)
      :progcaster progcaster
      :pointstamp-tracker tracker
      :shared-progress (op/shared-progress)
      :scope-summary scope-summary})))
