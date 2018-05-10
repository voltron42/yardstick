(ns yardstick.tags
  (:require [instaparse.core :as insta]
            [clojure.spec.alpha :as s]))

(s/def ::expr (s/or :tag ::tag
                    :not ::not
                    :and ::and
                    :or ::or))

(s/def ::not (s/and vector?
                    (s/cat :label #{:not}
                           :content (partial re-matches #"[a-zA-Z][a-zA-Z0-9]+"))))

(s/def ::not (s/and vector?
                    (s/cat :label #{:not}
                           :content ::expr)))

(s/def ::and (s/and vector?
                    (s/cat :label #{:and}
                           :content (s/and set?
                                           (s/coll-of ::expr)))))

(s/def ::or (s/and vector?
                   (s/cat :label #{:or}
                          :content (s/and set?
                                          (s/coll-of ::expr)))))

(defmulti ^:private build-tag-validator first)

(defmethod ^:private build-tag-validator :and [[_ args]]
  (let [args (map build-tag-validator args)]
    (fn [tag-set]
      (reduce #(and %1 (%2 tag-set)) true args))))

(defmethod ^:private build-tag-validator :or [[_ args]]
  (let [args (map build-tag-validator args)]
    (fn [tag-set]
      (reduce #(or %1 (%2 tag-set)) false args))))

(defmethod ^:private build-tag-validator :not [[_ expr]]
  (let [base (build-tag-validator expr)]
    (fn [tag-set]
      (not (base tag-set)))))

(defmethod ^:private build-tag-validator :tag [[_ tag]]
  (fn [tag-set]
    (contains? tag-set tag)))

(defmethod ^:private build-tag-validator :empty [_]
  (constantly true))

(defmulti ^:private parse-tag-expr first)

(defmethod ^:private parse-tag-expr :expression [[_ expr]]
  (parse-tag-expr expr))

(defmethod ^:private parse-tag-expr :sub-expression [[_ & args]]
  (let [root (first (filter vector? args))]
    (parse-tag-expr root)))

(defmethod ^:private parse-tag-expr :and [[_ & args]]
  (let [args (map parse-tag-expr (filter vector? args))]
    [:and (set args)]))

(defmethod ^:private parse-tag-expr :or [[_ & args]]
  (let [args (map parse-tag-expr (filter vector? args))]
    [:or (set args)]))

(defmethod ^:private parse-tag-expr :not [[_ _ expr]]
  [:not (parse-tag-expr expr)])

(defmethod ^:private parse-tag-expr :tag [expr]
  expr)

(defmethod ^:private parse-tag-expr :empty [expr]
  expr)

(def ^:private parse-tags
  (insta/parser
    "expression = and | or | not | tag | empty
    and = sub-expression (' & ' sub-expression)+
    or = sub-expression (' | ' sub-expression)+
    sub-expression = ('(' and ')') | ('(' or ')') | not | tag
    not = '!' sub-expression
    tag = #'[a-zA-Z][a-zA-Z0-9]+'
    empty = ''"))

(defn parse-tag-set [expression]
  (parse-tag-expr (parse-tags expression)))

(defn make-tag-validator [tag-set]
  (build-tag-validator tag-set))
