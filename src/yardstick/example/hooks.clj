(ns yardstick.example.hooks
  (:require [yardstick.core :as y]
            [clojure.test :refer :all])
  (:import (org.junit Assert)))

(def example-hooks
  (reify y/Hooks
    (before-suite [_]
      ;TODO
      )
    (after-suite [_]
      ;TODO
      (throw (Exception. "This is an example exception"))
      )
    (before-spec [_]
      ;TODO
      )
    (after-spec [_]
      ;TODO
      (Assert/assertTrue "This is also an example exception" false)
      )
    (before-scenario [_]
      ;TODO
      )
    (after-scenario [_]
      ;TODO
      (throw (Exception. "This is another example exception"))
      )))

