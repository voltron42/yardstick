(ns yardstick.spec-model
  (:require [clojure.spec.alpha :as s]))

(s/def ::spec (s/cat :spec-header ::spec-header
                     :tags (s/? ::tags)
                     :for-each (s/? ::steps)
                     :scenarios (s/+ ::scenario)))

(s/def ::scenario (s/cat :scenario-header ::scenario-header
                         :tags (s/? ::tags)
                         :steps ::steps))

(s/def ::steps (s/and vector?
                      (s/cat :label #{:ul}
                             :step (s/+ ::step))))

(s/def ::spec-header (s/and vector?
                            (s/cat :label #{:h1}
                                   :header string?)))

(s/def ::scenario-header (s/and vector?
                                (s/cat :label #{:h2}
                                       :header string?)))

(s/def ::tags (s/and vector?
                     (s/cat :label #{:h4}
                            :tags string?)))

(s/def ::step (s/and vector?
                     (s/cat :label #{:li}
                            :step (s/+ string?))))
