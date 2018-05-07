(ns yardstick.example.steps
  (:require [yardstick.core :as y]
            [clojure.pprint :as pp]))

(defmethod y/do-step "On the customer page" [_]
  ;TODO
  )

(defmethod y/do-step "Search for customer %1s" [_ customer-name]
  ;TODO
  )

(defmethod y/do-step "The customer %1s is listed" [_ customer-name]
  ;TODO
  )

(defmethod y/do-step "Find and Open product page for %1s" [_ product]
  ;TODO
  )

(defmethod y/do-step "Verify product %1s as %2s" [_ property value]
  ;TODO
  )

(defmethod y/do-step "Delete product %1s" [_ product]
  ;TODO
  )

(defmethod y/do-step "Create a product" [_ table]
  (println "I have a table of stuff!")
  (pp/pprint table))