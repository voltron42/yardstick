(ns yardstick.parse-test
  (:require [clojure.test :refer :all]
            [yardstick.parse :refer :all]
            [clojure.pprint :as pp]))

(deftest test-parse-customer
  (pp/pprint )
  (is (= (parse-test-file (slurp "resources/examples/customer.spec"))
        {:spec "Customers",
         :tags #{},
         :for-each [["On the customer page"]],
         :scenarios
         [{:scenario "Search for a customer",
           :tags #{"admin" "customer" "search"},
           :steps
                     [["Search for customer %1s" "ScroogeMcduck"]
                      ["The customer %1s is listed" "ScroogeMcduck"]]}
          {:scenario "Verify a bunch of customers",
           :tags #{"admin" "customer" "search"},
           :steps
                     [["Search for customers &lt;table:resources/user.csv&gt;"]]}]}))
  )

(deftest test-parse-products
  (pp/pprint (parse-test-file (slurp "resources/examples/products.spec")))
  (is (= (parse-test-file (slurp "resources/examples/products.spec"))
         ))
  )

(deftest test-parse-user
  (is (= (parse-test-file (slurp "resources/examples/user.spec"))
         {:spec "Signup",
          :tags #{},
          :for-each [],
          :scenarios
          [{:scenario "Register a customer",
            :tags #{"high" "user" "signup" "final" "smoke"},
            :steps
                      [["Sign up a new customer"]
                       ["On the customer page"]
                       ["Just registered customer is listed"]]}]}))
  )