(ns yardstick.example.hooks
  (:require [yardstick.core :as y]
            [clojure.test :refer :all])
  (:import (org.junit Assert)))

(def example-hooks
  (reify y/Hooks
    (before-spec [_ spec]
      ;TODO
      )
    (after-spec [_ spec]
      ;TODO
      (Assert/assertTrue "This is also an example exception" false)
      )
    (before-scenario [_ spec scenario]
      ;TODO
      )
    (after-scenario [_ spec scenario]
      ;TODO
      (throw (Exception. "This is another example exception"))
      )
    (before-step [_ spec scenario step]
      ;TODO
      )
    (after-step [_ spec scenario step]
      ;TODO
      )
    ))

