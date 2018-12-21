(ns differentiae.progress.nested.reachability-test
  (:require [clojure.test :refer :all]
            [differentiae.progress.frontier :as front]
            [differentiae.progress.nested.subgraph :as sg]
            [differentiae.progress.nested.reachability :as reach]))

(deftest example-reachability
  (let [builder (-> (reach/builder)

                    (reach/add-node 0 1 1 [[(front/antichain [0])]])
                    (reach/add-node 1 1 1 [[(front/antichain [0])]])
                    (reach/add-node 2 1 1 [[(front/antichain [1])]])

                    (reach/add-edge (sg/source 0 0) (sg/target 1 0))
                    (reach/add-edge (sg/source 1 0) (sg/target 2 0))
                    (reach/add-edge (sg/source 2 0) (sg/target 0 0)))]

    (clojure.pprint/pprint (reach/summarize builder))
    (let [tracker (reach/tracker (reach/summarize builder))]
      
      )
    
    
    )

  )
