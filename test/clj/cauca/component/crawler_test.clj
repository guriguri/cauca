(ns cauca.component.crawler-test
  (:use [clojure test]      
        ) 
  (:require [cauca.factory :as f]
            [cauca.component.crawler :as crawler]
            [cauca.log :as log]
            [cauca.config :as config]
            [clj-http.client :as client]
            )
  )

(deftest add-courtauctions-test
  (log/configure-logback "/cauca-logback.xml")
  (config/config-yaml "/cauca-context.yaml") 
  (let [dao-impl# (f/get-obj :courtauction-dao)
        sido (first (config/get-value :location.SidoCd))]
    (crawler/add-courtauctions! dao-impl# sido 20)
    )
  )