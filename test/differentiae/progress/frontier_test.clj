(ns differentiae.progress.frontier-test
  (:require [clojure.test :refer :all]
            [differentiae.progress.frontier :refer :all]))

(deftest test:mutable-antichains
  (let [ma (mutable-antichain)]
    (is (is-empty? ma)))

  (let [ma (mutable-antichain 1)]
    (apply-updates! ma [[1 -1] [2 1]])
    (is (= (frontier ma) (->AntichainImpl [2]))))

  (let [ma (mutable-antichain 1)
        changes (atom [])]
    (apply-updates-and! ma
                        [[1, -1] [2, 1]]
                        (fn [time diff] (swap! changes conj [time diff])))
    (is (= [[1, -1], [2, 1]] @changes))))

