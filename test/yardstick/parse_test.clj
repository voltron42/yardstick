(ns yardstick.parse-test
  (:require [clojure.test :refer :all]
            [yardstick.parse :refer :all]
            [clojure.pprint :as pp]))

(deftest test-parse-customer
  (pp/pprint (parse-test-file (slurp "resources/examples/customer.spec")))
  )

(deftest test-parse-products
  (pp/pprint (parse-test-file (slurp "resources/examples/products.spec")))
  )

(deftest test-parse-user
  (pp/pprint (parse-test-file (slurp "resources/examples/user.spec")))
  )