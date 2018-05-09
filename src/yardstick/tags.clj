(ns yardstick.tags
  (:require [instaparse.core :as insta]
            [clojure.pprint :as pp]))

(defmulti ^:private build-tag-validator first)

(defmethod ^:private build-tag-validator :expression [[_ root]]
  (build-tag-validator root))

(defmethod ^:private build-tag-validator :sub-expression [[_ & args]]
  (let [root (first (filter vector? args))]
    (build-tag-validator root)))

(defmethod ^:private build-tag-validator :and [[_ & args]]
  (let [args (map build-tag-validator (filter vector? args))]
    (fn [tag-set]
      (reduce #(and %1 (%2 tag-set)) ((first args) tag-set) (rest args)))))

(defmethod ^:private build-tag-validator :or [[_ & args]]
  (let [args (map build-tag-validator (filter vector? args))]
    (fn [tag-set]
      (reduce #(or %1 (%2 tag-set)) ((first args) tag-set) (rest args)))))

(defmethod ^:private build-tag-validator :not [[_ _ expr]]
  (let [base (build-tag-validator expr)]
    (fn [tag-set]
      (not (base tag-set)))))

(defmethod ^:private build-tag-validator :tag [[_ tag]]
  (fn [tag-set]
    (contains? tag-set tag)))

(def ^:private parse-tags
  (insta/parser
    "expression = and | or | not | tag
    and = sub-expression (' & ' sub-expression)+
    or = sub-expression (' | ' sub-expression)+
    sub-expression = ('(' and ')') | ('(' or ')') | not | tag
    not = '!' sub-expression
    tag = #'[a-zA-Z][a-zA-Z0-9]+'"))

(defn parse-tag-validator [expression]
  (if (empty? expression)
    (constantly true)
    (build-tag-validator (parse-tags expression))))
