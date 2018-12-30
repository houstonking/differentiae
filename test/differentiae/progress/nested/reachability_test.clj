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
                    (reach/add-node! 3 1 1 [[(front/antichain [1])]])
                    
                    (reach/add-edge! (loc/source 0 0) (loc/target 1 0))
                    (reach/add-edge! (loc/source 1 0) (loc/target 2 0))
                    (reach/add-edge! (loc/source 2 0) (loc/target 0 0)))
        [tracker _] (reach/build! builder)]
    (reach/update-source! tracker (loc/source 0 0) 17 1)
    (clojure.pprint/pprint tracker)
    (reach/propagate-all! tracker)
    (let [results (-> (reach/pushed tracker)
                      deref
                      set)]
      (is (= #{{:pointstamp {:location (loc/target 0 0) :time 18}
                :delta -1}
               {:pointstamp {:location (loc/target 1 0) :time 17}
                :delta -1}
               {:pointstamp {:location (loc/target 2 0) :time 17}
                :delta -1}}
             results)))))
