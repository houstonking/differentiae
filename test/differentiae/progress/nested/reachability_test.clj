(ns differentiae.progress.nested.reachability-test
  (:require [clojure.test :refer :all]
            [differentiae.progress.frontier :as front]
            [differentiae.progress.location :as loc]
            [differentiae.progress.nested.reachability :as reach]))

(deftest example-reachability
  (let [builder (-> (reach/builder)

                    (reach/add-node! 0 1 1 [[(front/antichain [0])]])
                    (reach/add-node! 1 1 1 [[(front/antichain [0])]])
                    (reach/add-node! 2 1 1 [[(front/antichain [1])]])                    
                    
                    (reach/add-edge! (loc/source 0 0) (loc/target 1 0))
                    (reach/add-edge! (loc/source 1 0) (loc/target 2 0))
                    (reach/add-edge! (loc/source 2 0) (loc/target 0 0)))
        [tracker _] (reach/build! builder)]
    (reach/update-source! tracker (loc/source 0 0) 17 1)
    (reach/propagate-all! tracker)
    (let [results (->> (reach/drain-pushed! tracker)                       
                       (filter (comp loc/target? ffirst))
                       sort)]
      (is (= [[[(loc/target 0 0) 18] 1]
              [[(loc/target 1 0) 17] 1]
              [[(loc/target 2 0) 17] 1]]
             results)))

    (reach/update-source! tracker (loc/source 0 0) 17 -1)
    (reach/propagate-all! tracker)
    (let [results (->> (reach/drain-pushed! tracker)                       
                       (filter (comp loc/target? ffirst))
                       sort)]
      (is (= [[[(loc/target 0 0) 18] -1]
              [[(loc/target 1 0) 17] -1]
              [[(loc/target 2 0) 17] -1]]
             results)))
    ))
