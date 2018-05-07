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
                            :step (s/+ string?)
                            :table (s/? ::table))))

(s/def ::table (s/and vector?
                      (s/cat :label #{:table}
                             :head ::table-head
                             :body ::table-body)))

(s/def ::table-head (s/and vector?
                           (s/cat :label #{:thead}
                                  :row ::table-header-row)))

(s/def ::table-header-row (s/and vector?
                                 (s/cat :label #{:tr}
                                        :headers (s/+ ::table-header))))

(s/def ::table-header (s/and vector?
                             (s/cat :label #{:th}
                                    :attrs (s/? (s/keys :req-un [::align]))
                                    :value string?)))

(s/def ::table-body (s/and vector?
                           (s/cat :label #{:tbody}
                                  :rows (s/+ ::table-row))))


(s/def ::table-row (s/and vector?
                          (s/cat :label #{:tr}
                                 :cells (s/+ ::table-cell))))

(s/def ::table-cell (s/and vector?
                           (s/cat :label #{:td}
                                  :attrs (s/? (s/keys :req-un [::align]))
                                  :value string?)))

(s/def ::align #{"center" "right" "left"})