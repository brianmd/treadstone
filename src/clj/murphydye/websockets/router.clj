(ns murphydye.websockets.router
  (:require [clojure.tools.logging :as log]
            [murphydye.utils.core :refer :all]
            ))

(defprotocol Dispatcher
  (dispatch [this v])
  (add [this key func]))

(def ^:dynamic *router-ancestors* '())

(defrecord Router [routes]
  Dispatcher
  (dispatch [this v]
    (log/info "about to dispatch in router " v)
    (let [app-name (first v)
          sub-dispatcher ((deref (:routes this)) app-name)
          ]
      (if-not sub-dispatcher
        (throw (Exception. (str "no such app-name " app-name))))
      (binding [*router-ancestors* (conj *router-ancestors* app-name)]
        (dispatch sub-dispatcher (rest v)))))
  (add [this key val]
    (swap! routes assoc key val)))

(defn make-router [] (->Router (atom {})))



(defrecord Actor [actions state]
  Dispatcher
  (dispatch [this event]
    (log/info "Actor.dispatch " event)
    (let [action-name (first event)
          func ((deref (:actions this)) action-name)]
      (if-not func
        (throw (Exception. (str "no such action " action-name))))
      (func state event)))
  (add [this event-name func] (swap! (:actions this) assoc event-name func)))

(defn make-actor [initial-state] (->Actor (atom {}) initial-state))


(def root-router (make-router))
;; (def root-router (make-router (atom {:connections {}})))

;; see timer.clj for example

(examples
 (keys (deref (:routes root-router)))
 (keys @(:routes root-router))
 (:stress-test @(:routes root-router))
 (keys @(:actions (:stress-test @(:routes root-router))))
 )

