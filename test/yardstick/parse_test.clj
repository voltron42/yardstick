(ns yardstick.parse-test
  (:require [clojure.test :refer :all]
            [yardstick.parse :refer :all]))

(deftest test-parse-customer
  (is (= (parse-test-file (slurp "resources/customer.spec"))
        {:spec "Customers",
         :tags #{},
         :for-each [["On the customer page"]],
         :scenarios
         [{:scenario "Search for a customer",
           :tags #{"admin" "customer" "search"},
           :steps [["Search for customer %1s" "ScroogeMcduck"]
                   ["The customer %1s is listed" "ScroogeMcduck"]]}
          {:scenario "Verify a bunch of customers",
           :tags #{"admin" "customer" "search"},
           :steps [["Search for customers"
                    [{"# users" "FredFlintstone"}
                     {"# users" "JohnnyQuest"}
                     {"# users" "ScroogeMcduck"}]]]}]})))

(deftest test-parse-products
  (is (= (parse-test-file (slurp "resources/products.spec"))
         {:spec "Products",
          :tags #{},
          :for-each [],
          :scenarios
          [{:scenario "Create a new product",
            :tags #{"admin" "product" "create"},
            :steps [["Create a product"
                     [{"Author"      "John P. Baugh"
                       "Description" "ISBN: 978-1453636671"
                       "Price"       "25.00"
                       "Title"       "Go Programming"}
                      {"Author"      "Ivo Balbaert"
                       "Description" "ISBN: 978-1469769165"
                       "Price"       "20.00"
                       "Title"       "The Way to Go"}
                      {"Author"      "Brian Ketelsen"
                       "Description" "ISBN: 9781617291784"
                       "Price"       "30.00"
                       "Title"       "Go In Action"}
                      {"Author"      "Miek Gieben"
                       "Description" "ebook"
                       "Price"       "0.00"
                       "Title"       "Learning Go"}]]]}
           {:scenario "Search for a product",
            :tags #{"admin" "product" "search"},
            :steps [["Find and Open product page for %1s" "Go Programming"]
                    ["Verify product %1s as %2s" "author" "John P. Baugh"]]}
           {:scenario "Search and edit and existing product",
            :tags #{"admin" "product" "edit"},
            :steps [["Open product edit page for stored productId"]
                    ["Update product specifier to new value"
                     [{"specifier" "title"
                       "value"     "The Way to Go On"}
                      {"specifier" "author"
                       "value"     "Ivo Jay Balbaert"}
                      {"specifier" "description"
                       "value"     "hohoho"}]]
                    ["Check product specifier has new value"
                     [{"specifier" "title"
                       "value"     "The Way to Go On"}
                      {"specifier" "author"
                       "value"     "Ivo Jay Balbaert"}
                      {"specifier" "description"
                       "value"     "hohoho"}]]]}
           {:scenario "Delete a product",
            :tags #{"delete" "admin" "product"},
            :steps [["Delete product %1s" "Learning Go"]]}]})))

(deftest test-parse-user
  (is (= (parse-test-file (slurp "resources/user.spec"))
         {:spec "Signup",
          :tags #{},
          :for-each [],
          :scenarios
          [{:scenario "Register a customer",
            :tags #{"high" "user" "signup" "final" "smoke"},
            :steps [["Sign up a new customer"]
                    ["On the customer page"]
                    ["Just registered customer is listed"]]}]})))
