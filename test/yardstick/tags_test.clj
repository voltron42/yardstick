(ns yardstick.tags-test
  (:require [clojure.test :refer :all]
            [yardstick.tags :refer :all]
            [clojure.set :as set]
            [clojure.pprint :as pp]))

(def ^:private expressions ["" "!TagA" "TagA & TagB" "TagA & !TagB" "TagA | TagB" "(TagA & TagB) | TagC" "!(TagA & TagB) | TagC" "(TagA | TagB) & TagC"])

(def ^:private base-tag-set #{"TagA" "TagB" "TagC"})

(defn- power-set [base-set]
  (if (empty? base-set)
    #{#{}}
    (let [p (first base-set)
          r (power-set (rest base-set))]
      (set/union r (set (map #(conj % p) r))))))

(deftest test-parse-tags
  (let [tag-sets (reduce #(assoc %1 %2 (parse-tag-set %2)) {} expressions)]
    (is (= tag-sets
           {"!(TagA & TagB) | TagC" [:or
                                     #{[:not
                                        [:and
                                         #{[:tag
                                            "TagA"]
                                           [:tag
                                            "TagB"]}]]
                                       [:tag
                                        "TagC"]}]
            "!TagA"                 [:not
                                     [:tag
                                      "TagA"]]
            ""                      [:empty]
            "(TagA & TagB) | TagC"  [:or
                                     #{[:and
                                        #{[:tag
                                           "TagA"]
                                          [:tag
                                           "TagB"]}]
                                       [:tag
                                        "TagC"]}]
            "(TagA | TagB) & TagC"  [:and
                                     #{[:or
                                        #{[:tag
                                           "TagA"]
                                          [:tag
                                           "TagB"]}]
                                       [:tag
                                        "TagC"]}]
            "TagA & !TagB"          [:and
                                     #{[:not
                                        [:tag
                                         "TagB"]]
                                       [:tag
                                        "TagA"]}]
            "TagA & TagB"           [:and
                                     #{[:tag
                                        "TagA"]
                                       [:tag
                                        "TagB"]}]
            "TagA | TagB"           [:or
                                     #{[:tag
                                        "TagA"]
                                       [:tag
                                        "TagB"]}]}))))

(deftest test-parse-tag-validator
  (let [validators (reduce #(assoc %1 %2 (make-tag-validator (parse-tag-set %2))) {} expressions)]
    (is (= (into {} (for [x expressions y (power-set base-tag-set)]
                      [[x y] ((get validators x) y)]))
               {["" #{"TagA" "TagB" "TagC"}] true
                ["" #{"TagA" "TagB"}] true
                ["" #{"TagA" "TagC"}] true
                ["" #{"TagA"}] true
                ["" #{"TagB" "TagC"}] true
                ["" #{"TagB"}] true
                ["" #{"TagC"}] true
                ["" #{}] true
                ["!(TagA & TagB) | TagC" #{"TagC"}] true,
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
