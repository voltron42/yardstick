(ns example.steps
  (:require [yardstick.core :as y]
            [clojure.pprint :as pp]))

(defn def-steps []
  (y/def-step "On the customer page" []
                ;TODO
                )

  (y/def-step "Search for customer %1s"
              [customer-name]
                ;TODO
                )

  (y/def-step "The customer %1s is listed"
              [customer-name]
                ;TODO
                )

  (y/def-step "Find and Open product page for %1s"
              [product]
                ;TODO
                )

  (y/def-step "Verify product %1s as %2s"
              [property value]
                ;TODO
                )

  (y/def-step "Delete product %1s"
              [product]
                ;TODO
                )

  (y/def-step "Create a product"
              [table]
                (println "I have a table of stuff!")
                (pp/pprint table))

  (y/def-step "Search for customers"
              [table]
                (println "I have a table of users!")
                (pp/pprint table))

  (y/def-step "Search for customers"
              [table]
                (println "I have a table of users!")
                (pp/pprint table))

  (y/def-step "Update product specifier to new value"
              [table]
                (println "I have a table of products!")
                (pp/pprint table))

  (y/def-step "Check product specifier has new value"
              [table]
                (println "I have a table of products!")
                (pp/pprint table))
  )
