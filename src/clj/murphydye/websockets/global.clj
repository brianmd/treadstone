(ns murphydye.websockets.global
  (:require [clojure.tools.logging :as log]
            [immutant.web.async :as async]
            [cognitect.transit :as transit]
            [clj-time.core :as t]
            [clojure.set :refer [difference]]

            [murphydye.utils.core :refer :all]
            [murphydye.websockets.router :as r :refer [add]]
            [murphydye.websockets.core :as ws]
            ))

(defn websockets-info [router]
  {:router router
   :connections (atom {})
   })

(defn create-actor [actor-name]
  (let [actor (r/make-router actor-name
                             (atom (websockets-info r/root-router)))]
    (add actor :new-client (fn [& args] (println "in websocket-actor" " new-client:" )))
    actor
    ))



