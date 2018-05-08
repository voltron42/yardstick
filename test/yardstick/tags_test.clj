(ns yardstick.tags-test
  (:require [clojure.test :refer :all]
            [yardstick.tags :refer :all]
            [clojure.set :as set]
            [clojure.pprint :as pp]))

(def ^:private expressions ["!TagA" "TagA & TagB" "TagA & !TagB" "TagA | TagB" "(TagA & TagB) | TagC" "!(TagA & TagB) | TagC" "(TagA | TagB) & TagC"])

(def ^:private base-tag-set #{"TagA" "TagB" "TagC"})

(defn- power-set [base-set]
  (if (empty? base-set)
    #{#{}}
    (let [p (first base-set)
          r (power-set (rest base-set))]
      (set/union r (set (map #(conj % p) r))))))

(deftest test-tag-parsing
  (let [validators (reduce #(assoc %1 %2 (parse-tag-validator %2)) {} expressions)]
    (is (= (into {} (for [x expressions y (power-set base-tag-set)]
                      [[x y] ((get validators x) y)]))
               {["!(TagA & TagB) | TagC" #{"TagC"}] true,
                ["TagA & !TagB" #{"TagA" "TagC"}] true,
                ["(TagA & TagB) | TagC" #{"TagA" "TagC"}] true,
                ["(TagA & TagB) | TagC" #{"TagB"}] false,
                ["TagA & !TagB" #{"TagB"}] false,
                ["(TagA & TagB) | TagC" #{"TagB" "TagA" "TagC"}] true,
                ["TagA & !TagB" #{}] false,
                ["(TagA | TagB) & TagC" #{"TagB" "TagA" "TagC"}] true,
                ["TagA & TagB" #{"TagA"}] false,
                ["TagA & TagB" #{"TagB" "TagA"}] true,
                ["!(TagA & TagB) | TagC" #{"TagA" "TagC"}] true,
                ["TagA & TagB" #{"TagB"}] false,
                ["(TagA | TagB) & TagC" #{"TagB" "TagC"}] true,
                ["TagA & TagB" #{"TagA" "TagC"}] false,
                ["!TagA" #{"TagB" "TagC"}] true,
                ["TagA | TagB" #{"TagB" "TagA" "TagC"}] true,
                ["TagA & TagB" #{"TagB" "TagC"}] false,
                ["TagA & !TagB" #{"TagB" "TagA"}] false,
                ["TagA & !TagB" #{"TagB" "TagA" "TagC"}] false,
                ["!(TagA & TagB) | TagC" #{"TagB" "TagA" "TagC"}] true,
                ["!(TagA & TagB) | TagC" #{"TagB" "TagA"}] false,
                ["(TagA | TagB) & TagC" #{"TagB"}] false,
                ["!TagA" #{"TagB"}] true,
                ["!TagA" #{}] true,
                ["(TagA & TagB) | TagC" #{"TagA"}] false,
                ["!(TagA & TagB) | TagC" #{"TagB" "TagC"}] true,
                ["!TagA" #{"TagC"}] true,
                ["!TagA" #{"TagB" "TagA" "TagC"}] false,
                ["TagA & !TagB" #{"TagC"}] false,
                ["(TagA | TagB) & TagC" #{"TagC"}] false,
                ["TagA & !TagB" #{"TagA"}] true,
                ["TagA | TagB" #{"TagA"}] true,
                ["!TagA" #{"TagA"}] false,
                ["(TagA | TagB) & TagC" #{"TagA"}] false,
                ["TagA | TagB" #{"TagB" "TagC"}] true,
                ["TagA | TagB" #{"TagB" "TagA"}] true,
                ["TagA | TagB" #{"TagA" "TagC"}] true,
                ["!TagA" #{"TagB" "TagA"}] false,
                ["(TagA & TagB) | TagC" #{}] false,
                ["!(TagA & TagB) | TagC" #{}] true,
                ["(TagA & TagB) | TagC" #{"TagB" "TagA"}] true,
                ["TagA | TagB" #{"TagB"}] true,
                ["!(TagA & TagB) | TagC" #{"TagB"}] true,
                ["(TagA & TagB) | TagC" #{"TagC"}] true,
                ["TagA | TagB" #{"TagC"}] false,
                ["TagA & TagB" #{"TagC"}] false,
                ["(TagA | TagB) & TagC" #{"TagB" "TagA"}] false,
                ["TagA & TagB" #{"TagB" "TagA" "TagC"}] true,
                ["!TagA" #{"TagA" "TagC"}] false,
                ["TagA & !TagB" #{"TagB" "TagC"}] false,
                ["TagA | TagB" #{}] false,
                ["(TagA | TagB) & TagC" #{"TagA" "TagC"}] true,
                ["(TagA | TagB) & TagC" #{}] false,
                ["TagA & TagB" #{}] false,
                ["!(TagA & TagB) | TagC" #{"TagA"}] true,
                ["(TagA & TagB) | TagC" #{"TagB" "TagC"}] true}))))
