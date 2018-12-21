(ns differentiae.progress.nested.operator-state
  (:require [differentiae.progress.frontier :as f]
            [differentiae.progress.change-batch :as cb]
            [differentiae.progress.operate :as op]
            ))

(defrecord OperatorState [name
                          index
                          id
                          local
                          notify
                          inputs
                          outputs
                          recently-active
                          operator
                          edges
                          external
                          consumed-buffer
                          internal-buffer
                          produced-buffer
                          external-buffer
                          gis-capabilities
                          gis-summary
                          logging])

(defn empty-operator [logging]
  (map->OperatorState {:name "EmptyOperator"
                       :operator nil
                       :index 0
                       :id (Long/MAX_VALUE)
                       :local false
                       :inputs 0
                       :outputs 0
                       :recently-active true
                       :notify true
                       :edges []
                       :external []
                       :external-buffer []
                       :consumed-buffer []
                       :internal-buffer []
                       :produced-buffer []
                       :logging logging
                       :gis-capabilities []
                       :gis-summary []}))

(defn operator-state [operate index path identifier logging]  
  (-> (select-keys operate [:local :inputs :outputs :notify
                            :name])
      (merge {:operator operate}
              
      (map->OperatorState))))

(defn add-input  [operator-state]
  (-> operator-state
      (update :inputs inc)
      (update :extenal conj (f/mutable-antichain))
      (update :extneral-buffer conj (cb/change-batch))
      (update :consumed-buffer conj (cb/change-batch))))

(defn add-output [operator-state]
  (-> operator-state
      (update :outputs inc)
      (update :edges conj [])
      (update :internal-buffer conj (cb/change-batch))
      (update :produced-buffer conj (cb/change-batch))))

(defn set-external-summary
  [{:as operator-state :keys [inputs external external-buffer operator]}
   summaries capabilities]
  (cond-> (reduce
           (fn [os input]
             (-> os
                 (update :external
                         f/apply-updates-and!
                         (nth capabilities input)
                         (fn [t v] (cb/update t v)))))
           (range inputs))
    (some? operator)
    ((fn [os] (update os :operator
                      op/set-external-summary summaries (:external-buffer os))))))
